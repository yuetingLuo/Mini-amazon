<template>
	<div>
	  <br>
	  <b-button square @click="showClearCartModal()" class="ml-2" size="lg" variant="warning">Checkout Cart</b-button>
	  <br><br>
	  <b-form-input
		v-model="searchQuery"
		placeholder="Search by description..."
		class="mb-3"
	  ></b-form-input>

	  <b-table :items="filteredProducts" :fields="fields">
		<template #cell(show-pic)="data">
    		<img :src="data.item.base64" alt="Product Image" style="width: 100px; height: auto;">
  		</template>
		<template #cell(buy)="data">
		  <b-button @click="showModal(data.item)" variant="primary">Buy</b-button><br><br>
		  <b-button @click="showAddCartModal(data.item)" variant="warning">Add To Cart</b-button>
		</template>
	  </b-table>
	  <b-modal id="buy-modal" ref="buyModal" title="Place Order" body-class="position-static" ok-title="Place Order" @ok="placeOrder">
		<label>UPS User ID:
        <b-form-input number v-model.number="upsUserId" type="string" placeholder="UPS User ID" required></b-form-input>
		</label><br>
		<label>Quantity:
        <b-form-input number v-model.number="orderQuantity" type="number" placeholder="Quantity of Item" min="1" step="1" required></b-form-input>
		</label><br>
		<label>X of Your Address:
        <b-form-input number v-model.number="addressX" type="number" placeholder="X of Your Address" min="0" step="1" required></b-form-input>
		</label><br>
		<label>Y of Your Address
		<b-form-input number v-model.number="addressY" type="number" placeholder="Y of Your Address" min="0" step="1" required></b-form-input>
		</label>
	  </b-modal>

	  <b-modal id="add-to-cart-modal" ref="addToCartModal" title="Add To Cart" body-class="position-static" ok-title="Add To Cart" @ok="addToCart">
		<label>Quantity:
        <b-form-input number v-model.number="orderQuantity" type="number" placeholder="Quantity of Item" min="1" step="1" required></b-form-input>
		</label><br>
	  </b-modal>

	  <b-modal id="checkout-modal" ref="checkoutModal" title="Review Cart" body-class="position-static" ok-title="Place Orders" @ok="clearCart">
		<b-table :items="dedupCart" :fields="cartFields"></b-table>
		<label>UPS User ID:
        <b-form-input number v-model.number="upsUserId" type="string" placeholder="UPS User ID" required></b-form-input>
		</label><br>
		<label>X of Your Address:
        <b-form-input number v-model.number="addressX" type="number" placeholder="X of Your Address" min="0" step="1" required></b-form-input>
		</label><br>
		<label>Y of Your Address
		<b-form-input number v-model.number="addressY" type="number" placeholder="Y of Your Address" min="0" step="1" required></b-form-input>
		</label>
	  </b-modal>
	</div>
</template>
  
<script setup lang="ts">
import { BIconLockFill } from 'bootstrap-vue';
import { ref, onMounted, computed } from 'vue';

const products = ref([]);
const searchQuery = ref('');
const fields = ['id', 'description', { key: 'show-pic', label: '' }, { key: 'buy', label: '' } ];
const cartFields = ['description', 'quantity'];
const upsUserId = ref('');
const orderQuantity = ref(1);
const addressX = ref(0);
const addressY = ref(0);
const buyModal = ref(null);
const addToCartModal = ref(null);
const checkoutModal = ref(null);
const cart = ref([]);
let currentProduct = ref(null);

const dedupCart = computed(() => {
  return cart.value.reduce((acc, cartItem) => {
    const existingItem = acc.find(i => i.description === cartItem.description);
    if (existingItem) {
      existingItem.quantity += cartItem.quantity;
    } else {
      acc.push({ ...cartItem });
    }
    return acc;
  }, []);
});


const fetchProducts = async () => {
try {
	const response = await fetch('/api/list-product');
	if (!response.ok) throw new Error('Failed to fetch products');
	products.value = await response.json();
} catch (error) {
	console.error('Error fetching products:', error);
}
};

const filteredProducts = computed(() => {
  return products.value.filter(product =>
    product.description.toLowerCase().includes(searchQuery.value.toLowerCase())
  );
});

const showModal = (item) => {
	currentProduct.value = item;
	buyModal.value.show();
};

const showAddCartModal = (item) => {
	currentProduct.value = item;
	addToCartModal.value.show();
};

const showClearCartModal = () => {
	checkoutModal.value.show();
};

const addToCart = async () => {
	const cartItem = {
		id: currentProduct.value.id,
		description: currentProduct.value.description,
		quantity: orderQuantity.value
	};
	cart.value.push(cartItem);
	addToCartModal.value.hide();
	alert('Item added to cart!');	
};

const clearCart = async () => {
	let err = false;
	for (let i = 0; i < cart.value.length; i++) {
		const orderData = {
			upsUserId: upsUserId.value,
			productId: cart.value[i].id,
			quantity: cart.value[i].quantity,
			destX: addressX.value,
			destY: addressY.value,
			username: localStorage.getItem('username')
		};
		
		try {
			const response = await fetch('/api/create-order', {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify(orderData)
			});
			if (!response.ok) {
				err = true;
			}
		} catch (error) {
			console.error('Error placing order:', error);
		}
	}
	cart.value = [];
	checkoutModal.value.hide();
	if (err) {
		alert('Failure with checkout!');
	} else {
		alert('Cart checkout successfully!');
	}
};

const placeOrder = async () => {
	const orderData = {
		upsUserId: upsUserId.value,
		productId: currentProduct.value.id,
		quantity: orderQuantity.value,
		destX: addressX.value,
		destY: addressY.value,
		username: localStorage.getItem('username')
	};
	try {
		const response = await fetch('/api/create-order', {
		method: 'POST',
		headers: { 'Content-Type': 'application/json' },
		body: JSON.stringify(orderData)
		});
		if (!response.ok) {
			alert('All inputs are required!');
		} else {
			alert('Order placed successfully!');
		}
	} catch (error) {
		console.error('Error placing order:', error);
	}
};

onMounted(fetchProducts);
</script>
  

<template>
	<div>
	   <br>
	   <b-button @click="refresh">Refresh</b-button>
	   <br><br>
	   <b-form-input
		v-model="searchQuery"
		placeholder="Search by order ID..."
		class="mb-3"
	  ></b-form-input>
	  <b-table :items="filteredOrders" :fields="tableFields" :busy="isRefreshing">
	  </b-table>
	</div>
</template>
  
<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
const isRefreshing = ref(false);
const orders = ref([]);
const searchQuery = ref('');
const tableFields = [
  { key: 'id', label: 'Order ID' },
  { key: 'productid', label: 'Product Name' },
  { key: 'description', label: 'Product Description' },
  { key: 'quantity', label: 'Quantity' },
  { key: 'coordinates', label: 'Destination Coordinate' },
  { key: 'status', label: 'Status' },
  { key: 'id', label: 'Tracking Number' }
];

const filteredOrders = computed(() => {
	return orders.value.filter(order =>
		order.id.toString().includes(searchQuery.value)
  	);
});

const refresh = async() => {
	isRefreshing.value = true;
	await fetchOrders();
	isRefreshing.value = false;
};

const fetchOrders = async () => {
	const usernameData = { username: localStorage.getItem('username') };
	console.log(usernameData);
	try {
		const response = await fetch('/api/list-order', {
			method: 'POST',
			headers: { 'Content-Type': 'application/json' },
			body: JSON.stringify(usernameData)
			});
		if (!response.ok) throw new Error('Failed to fetch orders');
		console.log(response);
		const fetchedOrders = await response.json();
		orders.value = fetchedOrders.map(order => ({
		  id: order.id,
		  productid: order.product.id,
		  description: order.product.description, 
		  quantity: order.quantity,
		  coordinates: `(${order.destX}, ${order.destY})`,
		  status: order.status
		}));
	} catch (error) {
		console.error('Error fetching products:', error);
	}
};

onMounted(fetchOrders);
</script>
  

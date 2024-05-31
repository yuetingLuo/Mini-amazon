<template>
  <div>
    <b-navbar toggleable="lg" type="dark" variant="dark">
      <b-navbar-brand href="#">MiniAmazon</b-navbar-brand>      
    </b-navbar>
    <br>
    <b-button pill variant="outline-primary" to="/list-product" size="lg" class="ml-2 mr-2">Main</b-button>
    <b-button pill variant="outline-primary" to="/list-order" size="lg">Orders</b-button>
    <router-view />
  </div>
</template>

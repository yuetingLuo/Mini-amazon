<template>
    <div>
        <br><br>
      <input v-model="username" placeholder="Username"><br><br>
      <input v-model="password" type="password" placeholder="Password"><br><br>
      <b-button @click="login" variant="primary">Login</b-button><br><br>
      <b-button @click="register" variant="outline-primary">Register</b-button><br>
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref } from 'vue';
  import { useRouter } from 'vue-router';
  
  const router = useRouter();
  const username = ref('');
  const password = ref('');
  
  const register = () => {
    router.push('/register');
  };

  const login = async () => {
    const response = await fetch('/api/login', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        username: username.value,
        password: password.value
      })
    });
  
    if (response.ok) {
        alert('Login success');
        localStorage.setItem('username', username.value);
        router.push('/list-product');
    } else {

        alert('Login failed');
    }
  };
  </script>
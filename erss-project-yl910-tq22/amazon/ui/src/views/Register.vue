<template>
    <div>
        <br><br>
        <b-button @click="back" variant="outline-primary">Back</b-button><br><br>
      <input v-model="username" placeholder="Username"><br><br>
      <input v-model="password" type="password" placeholder="Password"><br><br>
      <b-button @click="register" variant="primary">Register</b-button><br><br>
    </div>
  </template>
  
  <script setup lang="ts">
  import { ref } from 'vue';
  import { useRouter } from 'vue-router';
  
  const router = useRouter();
  const username = ref('');
  const password = ref('');


  const back = () => {
    router.push('/login');
  };

  const register = async () => {
    const response = await fetch('/api/register', {
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
        alert('Register success');
        router.push('/login');
    } else {
        console.log(response);
        alert('Register failed');
    }
  };
  </script>
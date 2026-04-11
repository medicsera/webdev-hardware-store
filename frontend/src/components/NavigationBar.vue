<script setup lang="ts">

</script>

<template>
  <header class="app-header">
    <nav class="container">
      <router-link to="/">
        <img src="/small-logo.jpg" alt="" class="logo">
      </router-link>
      <button class="catalog" >
        <img src="@/assets/catalog-icon.svg" alt="" />
        <span class="catalog-label">Каталог</span>
      </button>
      <div class="search">
        <input id="search" class="search-input" placeholder="Поиск товара" type="text"/>
        <img src="@/assets/search-icon.svg" alt="" class="search-icon"/>
      </div>
      <div class="contacts">
        <div class="contacts-div">
          <span class="contacts-telephone">8-923-599-74-89</span>
        </div>
        <div class="contacts-icons">
          <img src="@/assets/telephone-icon.svg" alt=""/>
          <img src="@/assets/mail-icon.svg" alt=""/>
          <img src="@/assets/vk-icon.svg" alt=""/>
          <img src="@/assets/telegram-icon.svg" alt=""/>
        </div>
      </div>
      <div class="cart">
        <button class="cart-btn">
          <img src="@/assets/cart-icon.svg" alt="" />
        </button>
        <span class="cart-span">Корзина</span>
      </div>
      <div class="profile">
        <button class="profile-btn">
          <img src="@/assets/profile-icon.svg" alt="" />
        </button>
        <span class="profile-span">Профиль</span>
      </div>
    </nav>
  </header>
</template>

<style lang="scss" scoped>
.app-header{
  display: inline;
}

.container {
  display: flex;
  align-items: center;
  justify-content: space-around;
  margin: 30px 10%;
  height: 50px;
}

.logo {
  width: auto;
  height: 50px;
  border-radius: 10px;
  cursor: pointer;
}

.catalog{
  display: flex;
  align-items: center;
  justify-content: space-around;
  height: 100%;
  width: 150px;
  border: 2px;
  border-radius: 10px;
  font-size: $default-size;
  background-color: $light-orange-color;
  font-style: oblique;
  cursor: pointer;
}
.search {
  display: flex;
  align-items: center;
  height: 90%;
  border-radius: 10px;
  background-color: $light-grey-color;

  &-input {
    font-size: $default-size;
    width: 700px;
    border: 0;
    outline: 0;
    background: 0;
    margin-left: 20px;
  }

  &-icon {
    margin-right: 10px;
  }

}
.contacts {
  width: 150px;
  height: 100%;

  &-div {
    display: flex;
    justify-content: center;
  }

  &-telephone {
    font-size: $default-size;
  }

  &-icons {
    display: flex;
    justify-content: space-between;
    margin: 0 10%;
    cursor: pointer;
  }
}

.cart {
  display: grid;
  justify-content: center;
  align-content: center;
  width: 50px;
  height: 100%;

  &-btn {
    display: flex;
    justify-content: center;
    background: 0;
    border: 0;
    cursor: pointer;
  }

  &-span {
    justify-content: center;
    font-size: $small-size;
    cursor: default;
  }
}

.profile {
  display: grid;
  justify-content: center;
  align-content: center;
  width: 50px;
  height: 100%;

  &-btn {
    display: flex;
    justify-content: center;
    background: 0;
    border: 0;
    cursor: pointer;
  }

  &-span {
    justify-content: center;
    font-size: $small-size;
    cursor: default;
  }
}


</style>
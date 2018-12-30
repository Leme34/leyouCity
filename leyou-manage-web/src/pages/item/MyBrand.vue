<template>
  <div>
    <v-layout>
      <v-flex xs2>
        <v-btn color="info">新增品牌</v-btn>
      </v-flex>
      <v-spacer/>
      <!-- 搜索框 -->
      <v-flex xs4>
        <v-flex xs8>
          <v-text-field label="搜索" append-icon="search"></v-text-field>
        </v-flex>
      </v-flex>
    </v-layout>

    <!-- vuetify中的表单 -->
    <v-data-table
      :headers="headers"
      :items="brands"
      :pagination.sync="pagination"
      :total-items="totalBrands"
      :loading="loading"
      class="elevation-1"
    >
      <!-- slot:遍历的列表 -->
      <template slot="items" slot-scope="props">
        <td class="text-xs-center">{{ props.item.id }}</td>
        <td class="text-xs-center">{{ props.item.name }}</td>
        <td class="text-xs-center">
          <img :src="props.item.image">
        </td>
        <td class="text-xs-center">{{ props.item.letter }}</td>
        <td class="text-xs-center">
          <v-btn flat icon color="info">
            <v-icon>edit</v-icon>
          </v-btn>
          <v-btn flat icon color="error">
            <v-icon>delete</v-icon>
          </v-btn>
        </td>
      </template>
    </v-data-table>
  </div>
</template>


<script>

export default {
  name: "MyBrand",

  //定义此页的数据对象
  data() {
    return {
      headers: [
        //表头数组
        { text: "品牌id", value: "id", align: "center", soreable: true },
        { text: "品牌名称", value: "name", align: "center", soreable: false },
        { text: "品牌logo", value: "image", align: "center", soreable: false },
        {
          text: "品牌首字母",
          value: "letter",
          align: "center",
          soreable: true
        },
        { text: "操作", align: "center", soreable: false }
      ],
      brands: [],
      pagination: [],
      totalBrands: 0, //总品牌记录(行)数
      loading: false //是否显示加载动画
    };
  },

  //页面初始化方法
  created() {
    
    //axios发ajax请求,baseUrl的配置见http.js
    this.$http.get("/brand/page",{
        params:{
            page:1,
        }
    })
    .then(res=>{

    })

    
    this.brands = [
      { id: 1, name: "小米", image: "1.jpg", letter: "X" },
      { id: 2, name: "华为", image: "2.jpg", letter: "H" },
      { id: 3, name: "苹果", image: "3.jpg", letter: "P" }
    ];
    this.totalBrands = 15;
  }
};
</script>


<style scoped>
</style>
 
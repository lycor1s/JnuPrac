package com.qienys.JnuPrac.Controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qienys.JnuPrac.pojo.Cart;
import com.qienys.JnuPrac.pojo.User;
import com.qienys.JnuPrac.service.impl.CartServiceImpl;
import com.qienys.JnuPrac.service.impl.ProductServiceImpl;
import com.qienys.JnuPrac.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


import java.util.List;


@Controller
public class CartController {

    @Autowired
    private CartServiceImpl cartServiceImpl;
    @Autowired
    private ProductServiceImpl productServiceImpl;
    @Autowired
    private UserServiceImpl userServiceImpl;

    @PostMapping(value = "/addToCart", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String addToCart(@RequestBody JSONObject jsonParam) {
        //request productId  number
        Cart cart = JSON.parseObject(jsonParam.toJSONString(),Cart.class);
        //User loginUser = (User) SecurityUtils.getSubject().getPrincipal();
        User loginUser = userServiceImpl.findByUserName("user");
        cart.setUid(loginUser.getId());
        //若购物车存在此商品则修改数量 不存在则保存新的数据
        if(cartServiceImpl.existsByUidAndProductId(loginUser.getId(),cart.getProductId())){
            Cart tempCart = cartServiceImpl.findByUidAndAndProductId(loginUser.getId(),cart.getProductId());
            tempCart.setNumber(tempCart.getNumber()+cart.getNumber());
            cartServiceImpl.save(tempCart);
        }
        else{
            cartServiceImpl.save(cart);
        }
        JSONObject jsonObject=new JSONObject();
        jsonObject.put("msg","success");
        return jsonObject.toJSONString();
    }

    @RequestMapping(value = "/getCartList", method = RequestMethod.GET, produces = "application/json;charset = UTF-8")
    @ResponseBody
    public String getCartList(){
        //User loginUser = (User) SecurityUtils.getSubject().getPrincipal();
        User loginUser = userServiceImpl.findByUserName("user");
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        List<Cart> cartList = cartServiceImpl.findAllByUid(loginUser.getId());

        jsonObject.put("method", "json");
        jsonArray.add(jsonObject);
        jsonArray.add(cartList);

        return jsonArray.toJSONString();
    }

    @GetMapping("getTotalPrice")
    @ResponseBody
    public String getTotalPrice(){
        //User loginUser = (User) SecurityUtils.getSubject().getPrincipal();
        User loginUser = userServiceImpl.findByUserName("user");
        JSONObject jsonObject = new JSONObject();
        String totalPrice = Double.toString((cal_totalPrice(loginUser.getId())));
        jsonObject.put("totalPrice",totalPrice);
        return jsonObject.toJSONString();
    }

    public double cal_totalPrice(Long uid) {
        int sum = 0;
        List<Cart> cartList = cartServiceImpl.findAllByUid(uid);
        for(Cart cart : cartList) {
            double price = productServiceImpl.findById(cart.getProductId()).getPrice() *cart.getNumber();
            sum += price;
        }
        return sum;
    }
}
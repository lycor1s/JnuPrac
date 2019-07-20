package com.qienys.JnuPrac.Controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.qienys.JnuPrac.pojo.Cart;
import com.qienys.JnuPrac.pojo.OrderProducts;
import com.qienys.JnuPrac.pojo.Orders;
import com.qienys.JnuPrac.pojo.User;
import com.qienys.JnuPrac.service.impl.*;
import com.qienys.JnuPrac.util.snowflake;
import netscape.javascript.JSObject;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;


@Controller
public class OrderController {

    @Autowired
    private OrdersServiceImpl ordersServiceImpl;
    @Autowired
    private OrderProductsServiceImpl orderProductsServiceImpl;
    @Autowired
    private CartServiceImpl cartServiceImpl;
    @Autowired
    private UserServiceImpl userServiceImpl;
    @Autowired
    private ProductServiceImpl productServiceImpl;



    @PostMapping(value = "/orderGen", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String orderGen(@RequestBody JSONArray jsonParam){
        //telephone name address Cartlist

        //订单号生成
        snowflake idWorker = new snowflake(0,0);
        Long orderId = idWorker.nextId();

            // User loginUser = userServiceImpl.findByUserName("user");//test
        User loginUser = (User) SecurityUtils.getSubject().getPrincipal();

        //index 0 传给orders表
        JSONObject orderInfo = jsonParam.getJSONObject(0) ;
        jsonParam.remove(0);

        JSONObject jsonObject = new JSONObject();

        if(!jsonParam.isEmpty()){
            List<Cart> cartList = JSON.parseArray(jsonParam.toJSONString(),Cart.class);
            for(Cart cart : cartList) {
                //System.out.println("cart pid"+cart.getProductId()+" count"+cart.getCount());
                Cart tempCart = cartServiceImpl.findByUidAndAndProductId(loginUser.getId(),cart.getProductId());
                //System.out.println("tempcart pid"+tempCart.getProductId()+" count"+tempCart.getCount());
                orderProductsServiceImpl.save(
                        new OrderProducts (
                                orderId,
                                cart.getProductId(),
                                tempCart.getProductName(),
                                tempCart.getDescription(),
                                cart.getCount(),
                                tempCart.getPrice()));
                cartServiceImpl.deleteByUidAndProductId(loginUser.getId(),cart.getProductId());

            }

            double sum = 0.0;
            for(Cart cart : cartList) {
                double price = productServiceImpl.findById(cart.getProductId()).getPrice() * cart.getCount();
                sum += price;
            }

            Orders order = JSON.parseObject(orderInfo.toJSONString(),Orders.class);
            order.setOrderId(orderId);
            order.setPayStatus(false);
            order.setPayStatus(false);
            order.setUid(loginUser.getId());
            order.setTotalPrice(sum);
            order.setCreateTime(new Date());
            ordersServiceImpl.save(order);


            jsonObject.put("router","myOrders");//去看订单
            jsonObject.put("msg","success");
        }
        else{
            jsonObject.put("router","default");//去看订单
            jsonObject.put("msg","emptyList");
        }


        return jsonObject.toJSONString();
    }



    //获取用户的所有订单
    @ResponseBody
    @GetMapping(value = "/getUserOrders")
    public String getOrderList(){
        User loginUser = userServiceImpl.findByUserName("user");//test
        //User loginUser = (User) SecurityUtils.getSubject().getPrincipal();
        List<Orders> ordersList= ordersServiceImpl.findAllByUid(loginUser.getId());
        JSONArray jsonArray = new JSONArray();
        jsonArray.add(ordersList);
        return JSON.toJSONString(ordersList);
    }

    //根据订单号查商品
    @ResponseBody
    @GetMapping(value = "/getOrderProducts", produces = "application/json;charset=UTF-8")
    public String getOrderProducts(@RequestBody JSONObject jsonParam){
        //request orderId
        //User loginUser = userServiceImpl.findByUserName("user");//test
        User loginUser = (User) SecurityUtils.getSubject().getPrincipal();
        Orders tempOrder= JSON.parseObject(jsonParam.toJSONString(),Orders.class);
        Orders orders=ordersServiceImpl.findByOrderId(tempOrder.getOrderId());
        if(ordersServiceImpl.existsByOrderId(orders.getOrderId())&&
        orders.getUid()==loginUser.getId()){
            List<OrderProducts> orderProductsList= orderProductsServiceImpl.findAllByOrderId(orders.getOrderId());
            return JSON.toJSONString(orderProductsList);
        }
        else{
            JSONObject json = new JSONObject();
            json.put("msg","Wrong OrderId");
            return json.toJSONString();
        }
    }


    //admin api
    @ResponseBody
    @GetMapping(value = "/getAllOrders")
    public String getAllOrders(){
        User loginUser = (User) SecurityUtils.getSubject().getPrincipal();
        JSONObject jsonObject = new JSONObject();
        if(SecurityUtils.getSubject().isAuthenticated()){
            User user = (User) SecurityUtils.getSubject().getPrincipal();
            System.out.println(user.getUserName());
            if(loginUser.getUserType().equals("admin")) {
                List<Orders> ordersList = ordersServiceImpl.findAll();
                return JSON.toJSONString(ordersList);
            }
        }
        else {
            jsonObject.put("msg","UnAuthorization");
            jsonObject.put("router","404");
        }
        return jsonObject.toJSONString();
    }

    @ResponseBody
    @PostMapping(value = "/setOrderPay", produces = "application/json;charset=UTF-8")
    public String setOrderPay(@RequestBody JSONObject JsonParam){
        //request orderID
        User user = (User) SecurityUtils.getSubject().getPrincipal();
        Orders orders = JSON.parseObject(JsonParam.toJSONString(),Orders.class);
        Orders tempOrder = ordersServiceImpl.findByOrderId(orders.getOrderId());
        tempOrder.setPayStatus(true);
        ordersServiceImpl.save(tempOrder);
        JSONObject json = new JSONObject();
        json.put("msg","success");
        return json.toJSONString();
    }

    @ResponseBody
    @PostMapping(value = "/setOrderPost", produces = "application/json;charset=UTF-8")
    public String setOrderPost(@RequestBody JSONObject JsonParam){
        //request orderID
        User user = (User) SecurityUtils.getSubject().getPrincipal();
        Orders orders = JSON.parseObject(JsonParam.toJSONString(),Orders.class);
        Orders tempOrder = ordersServiceImpl.findByOrderId(orders.getOrderId());
        tempOrder.setPostStatus(true);
        ordersServiceImpl.save(tempOrder);
        JSONObject json = new JSONObject();
        json.put("msg","success");
        return json.toJSONString();
    }

}

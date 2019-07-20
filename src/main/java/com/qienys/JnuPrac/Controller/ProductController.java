package com.qienys.JnuPrac.Controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.qienys.JnuPrac.pojo.Product;
import com.qienys.JnuPrac.pojo.User;
import com.qienys.JnuPrac.service.impl.ProductServiceImpl;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@Controller
public class ProductController {
    @Autowired
    private ProductServiceImpl productServiceImpl;

    private static String UPLOADED_FOLDER = "src/main/resources/static/upload/";

    @GetMapping("/static/upload")
    public String upload() {
        return "upload";
    }

    @PostMapping("/static/upload") // //new annotation since 4.3
    @ResponseBody
    public String singleFileUpload(@RequestParam("file") MultipartFile file) {
        JSONObject json = new JSONObject();
        if (file.isEmpty()) {
            json.put("msg","empty file");
            return json.toJSONString();
        }

        try {
            // Get the file and save it somewhere
            byte[] bytes = file.getBytes();
            Path path = Paths.get(UPLOADED_FOLDER + file.getOriginalFilename());
            Files.write(path, bytes);
            json.put("msg","uploadSuccess");
            json.put("file name",file.getOriginalFilename().toString());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return json.toJSONString();
    }

    @GetMapping("/uploadStatus")
    public String uploadStatus() {
        return "uploadStatus";
    }



    //admin api
    @PostMapping(value = "/addProducts", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String addProducts(@RequestBody JSONObject jsonParam){
        Product product = JSON.parseObject(jsonParam.toJSONString(),Product.class);
        User user = (User) SecurityUtils.getSubject().getPrincipal();
        JSONObject json = new JSONObject();
        //只有管理员有权限
        if(user.getUserType().equals("admin")){
            if(productServiceImpl.existsByTypeIdAndBrandIdAndName(
                    product.getTypeId(),
                    product.getBrandId(),
                    product.getName())) {
                json.put("msg","product already exist");
                json.put("router","");
            }else {
                productServiceImpl.save(product);
                json.put("msg","success");
                json.put("router","");
            }
        }
        else {
            json.put("msg","UnAuthentication")  ;
        }


        return json.toJSONString();
    }

    @PostMapping(value = "/changeProducts", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String changeProducts(@RequestBody JSONObject jsonParam){
        Product product = JSON.parseObject(jsonParam.toJSONString(),Product.class);
        JSONObject json = new JSONObject();
        User user = (User) SecurityUtils.getSubject().getPrincipal();

        if(user.getUserType().equals("admin")) {
            Product tempProduct = productServiceImpl.
                    findByTypeIdAndBrandIdAndName(
                            product.getTypeId(),
                            product.getBrandId(),
                            product.getName());
            tempProduct.setBrandId(product.getBrandId());
            tempProduct.setActive(product.isActive());
            tempProduct.setUrl(product.getUrl());
            tempProduct.setTypeId(product.getTypeId());
            tempProduct.setStock(product.getStock());
            tempProduct.setSold(product.getSold());
            tempProduct.setPrice(product.getPrice());
            tempProduct.setName(product.getName());
            tempProduct.setDescription(product.getDescription());
            if (productServiceImpl.existsByTypeIdAndBrandIdAndName(
                    tempProduct.getTypeId(),
                    tempProduct.getBrandId(),
                    tempProduct.getName())) {

                json.put("msg", "productAlreadyExist");
            } else {
                productServiceImpl.save(tempProduct);
                json.put("msg", "changeSuccess");
            }
        }
        else {
            json.put("msg","UnAuthentication")  ;
        }
        return  json.toJSONString();
    }


}

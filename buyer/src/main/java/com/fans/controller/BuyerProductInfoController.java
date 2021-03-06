package com.fans.controller;

import com.fans.common.JsonData;
import com.fans.pojo.ProductCategory;
import com.fans.pojo.ProductInfo;
import com.fans.service.interfaces.IProductCategoryService;
import com.fans.service.interfaces.IProductInfoService;
import com.fans.vo.ProductInfoVO;
import com.fans.vo.ProductVO;
import com.google.common.collect.Lists;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @ClassName BuyerProductInfoController
 * @Description:
 * @Author fan
 * @Date 2019-01-03 10:22
 * @Version 1.0
 **/
@RestController
@RequestMapping(value = "/buyer/product")
@CacheConfig(cacheNames = "product")
@Api(value = "买家商品信息控制层", tags = "买家商品信息相关")
public class BuyerProductInfoController {
    @Resource(name = "iProductCategoryService")
    private IProductCategoryService productCategoryService;
    @Resource(name = "iProductInfoService")
    private IProductInfoService productInfoService;

    @GetMapping(value = "/list")
    @Cacheable(key = "123", unless = "#result.code !=0")
    public JsonData list() {
        //1. 查询上架商品
        List<ProductInfo> productInfoList = productInfoService.findUpAll();
        //2. 查询类目（一次性查询）
        List<Integer> cateGoryTypes = productInfoList.stream()
                .map(ProductInfo::getCategoryType)
                .collect(Collectors.toList());
        List<ProductCategory> categoryList = productCategoryService.findByCategoryTypeIn(cateGoryTypes);
        //3. 数据拼装
        List<ProductVO> result = Lists.newArrayList();
        for (ProductCategory category : categoryList) {
            List<ProductInfoVO> productInfoVOList = Lists.newArrayList();
            for (ProductInfo productInfo : productInfoList) {
                if (productInfo.getCategoryType().equals(category.getCategoryType())) {
                    ProductInfoVO productInfoVO = ProductInfoVO.builder().build();
                    BeanUtils.copyProperties(productInfo, productInfoVO);
                    productInfoVOList.add(productInfoVO);
                }
            }
            ProductVO productVO = ProductVO.builder()
                    .cateGoryName(category.getCategoryName())
                    .cateGoryType(category.getCategoryType())
                    .productInfoVOList(productInfoVOList)
                    .build();
            result.add(productVO);
        }
        return JsonData.success("成功", result);
    }

    @GetMapping(value = "/search")
    @ApiOperation(value = "买家商品页面通过名称搜索产品接口", tags = "买家商品信息相关")
    public JsonData<List<ProductInfoVO>> productSearch(@RequestParam(value = "proName", required = false) @ApiParam(value = "商品名称（型号）") String proName) {
        List<ProductInfo> productInfoList = productInfoService.searchProDuctUp(proName);
        if (CollectionUtils.isEmpty(productInfoList)) {
            return JsonData.success("暂无此型号商品！！！");
        }
        List<ProductInfoVO> result = Lists.newArrayList();
        ProductInfoVO productInfoVO;
        for (ProductInfo productInfo : productInfoList) {
            productInfoVO = ProductInfoVO.adapt(productInfo);
            result.add(productInfoVO);
        }
        return JsonData.success("查询成功", result);
    }
}

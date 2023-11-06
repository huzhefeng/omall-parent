package com.offcn.all.controller;

import com.offcn.common.result.Result;
import com.offcn.list.client.ListFeignClient;
import com.offcn.model.list.SearchParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ListController {

    @Autowired
    private ListFeignClient listFeignClient;

    //定义搜索列表
    @GetMapping("list.html")
    public String search(SearchParam searchParam, Model model) throws IOException {
        //调用搜索服务feign接口，发出搜索
        Result<Map> result = listFeignClient.list(searchParam);
        Map data = result.getData();
        model.addAllAttributes(data);

        //处理拼接url参数
        String urlParam = makeUrlParam(searchParam);
        //把拼接后url参数传递到前端
        model.addAttribute("urlParam",urlParam);
        //同时把搜索条件参数页传递给前端
        model.addAttribute("searchParam",searchParam);

        //处理品牌面包屑
        String trademark = makeTrademark(searchParam.getTrademark());
        //封装到model
        model.addAttribute("trademarkParam",trademark);

        //处理平台属性面包屑数据
        List<Map<String, String>> propsList = makeProps(searchParam.getProps());

        model.addAttribute("propsParamList",propsList);

        //调用排序处理方法
        Map<String, Object> orderMap = dealOrder(searchParam.getOrder());
        model.addAttribute("orderMap",orderMap);
        //跳转到模板-搜索列表页
        return "list/index";
    }

    //处理平台排序
    private Map<String, Object> dealOrder(String order){
        Map<String, Object> map=new HashMap<>();

        //判断排序条件是否为空
        if(!StringUtils.isEmpty(order)){
            // 1:asc 使用 ：切开变成数组
            String[] split = order.split(":");
            //判断数组是否为空 2
            if(split!=null&&split.length==2){
                //把数组值封装到map
                map.put("type",split[0]);
                map.put("sort",split[1]);
            }
        }else {
            map.put("type","1");
            map.put("sort","asc");
        }

        return map;
    }

    //处理平台属性面包屑
    private List<Map<String, String>> makeProps(String[] props){
        List<Map<String, String>> list=new ArrayList<>();

        //判断props数组是否为空
        if(props!=null&&props.length>0){
            //循环遍历平台属性查询条件数组
            for (String prop : props) {
                //单独处理条件 23:4G:运行内存
                //使用: 切开成数组
                String[] split = prop.split(":");
                if(split!=null&&split.length==3){
                    //创建一个Map封装平台属性值
                    Map<String, String> map=new HashMap<>();
                    map.put("attrId",split[0]);
                    map.put("attrValue",split[1]);
                    map.put("attrName",split[2]);
                    //把map加入到list
                    list.add(map);
                }
            }
        }

        return list;
    }

    //处理当前选中品牌，面包屑显示内容
    private String makeTrademark(String trademark){
        //判断品牌条件是否为空
        if(!StringUtils.isEmpty(trademark)){
            //使用 ：切开成数组
            String[] split = trademark.split(":");
            if(split!=null&&split.length==2){
                return "品牌:"+split[1];
            }
        }
        return "";
    }

    //定义记录请求参数，拼接到url
    private String makeUrlParam(SearchParam searchParam){
        //定义可以改变字符串
        StringBuilder urlParam = new StringBuilder();
        //判断keyword搜索关键字是否为空
        if(!StringUtils.isEmpty(searchParam.getKeyword())){
            //把keyword搜索关键字拼接到urlParam
            urlParam.append("keyword=").append(searchParam.getKeyword());
        }
        //判断一级分类编号是否为空
        if(!StringUtils.isEmpty(searchParam.getCategory1Id())){
            //把一级分类拼接到urlParam
            urlParam.append("category1Id=").append(searchParam.getCategory1Id());
        }
        if(!StringUtils.isEmpty(searchParam.getCategory2Id())){
            //把一级分类拼接到urlParam
            urlParam.append("category2Id=").append(searchParam.getCategory2Id());
        }
        if(!StringUtils.isEmpty(searchParam.getCategory3Id())){
            //把一级分类拼接到urlParam
            urlParam.append("category3Id=").append(searchParam.getCategory3Id());
        }

        //判断搜索条件是否包含品牌
        if(!StringUtils.isEmpty(searchParam.getTrademark())){
          //判断当前urlParam是否有值
            if(urlParam.length()>0){
                //在有值基础上拼接 品牌参数
                urlParam.append("&trademark=").append(searchParam.getTrademark());
            }else {
                urlParam.append("trademark=").append(searchParam.getTrademark());
            }
        }

        //判断搜索条件是否包含平台属性
        if(searchParam.getProps()!=null){
            //遍历平台属性数组
            for (String prop : searchParam.getProps()) {
                //23:4G:运行内存
                if(urlParam.length()>0){
                    urlParam.append("&props=").append(prop);
                }else {
                    urlParam.append("props=").append(prop);
                }

            }
        }

        return "list.html?"+urlParam.toString();
    }
}

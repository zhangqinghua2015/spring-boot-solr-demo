package com.zqh.solr.controller;

import com.alibaba.fastjson.JSON;
import com.zqh.solr.domian.Book;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.DocumentObjectBinder;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.noggit.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @description:
 * @author: zhangqinghua
 * @email: zidian.zqh@raycloud.com
 * @date: 2021/10/21 下午3:32
 */
@Controller
@RequestMapping("/book")
public class BookController {
    @Autowired
    SolrClient solrClient;

    /**
     * SolrAddTest和SolrUpdateTest两个方法是一致的，Solr的core有id为修改，没有id为添加 只是使用方法不同
     */
    @ResponseBody
    @RequestMapping("/add")
    public String add(Book book) {
        DocumentObjectBinder binder = new DocumentObjectBinder();
        SolrInputDocument document = binder.toSolrInputDocument(book);
//        SolrInputDocument document = new SolrInputDocument();
        document.setField("id", book.getId());
        document.setField("description", book.getDescription());
        try {
            solrClient.add("book", document);
            solrClient.commit();
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
            return "Exception";
        }
        return "ok";
    }


    @ResponseBody
    @RequestMapping("/update")
    public Book update(Book book) {
        try {
            solrClient.addBean("book", book);
            solrClient.commit();
        } catch (IOException | SolrServerException e) {
            e.printStackTrace();
        }
        return book;
    }

    /**
     * 删除
     */
    @ResponseBody
    @RequestMapping("/delete")
    public String delete(String query) {
        try {
//            solrClient.deleteById(query);//根据id删除
            solrClient.deleteByQuery("book", query);//根据索引查询删除  *:*   description:你在什么地方
            solrClient.commit();
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
            return "Exception";
        }
        return "ok";
    }

    @ResponseBody
    @RequestMapping("/queryAll")
    public List<Book> queryAll() {
        List<Book> bookList = null;
        SolrQuery solrQuery = new SolrQuery();
        solrQuery.setQuery("*:*");
        try {
            QueryResponse queryResponse = solrClient.query("book", solrQuery);
            if (queryResponse != null) {
                bookList = queryResponse.getBeans(Book.class);
            }
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
        }
        return bookList;
    }

    /**
     * 返回搜索指定内容加高亮
     */
    @ResponseBody
    @RequestMapping("/query")
    public List<Book> query(String query) {
        List<Book> bookList = new ArrayList<Book>();
        SolrQuery solrQuery = new SolrQuery();
        //设置默认搜索的域(字段,只能set一个，solrQuery.set("df", "xxx");)
        solrQuery.set("df", "description");
        solrQuery.setQuery(query);
        SolrQuery solrParams = new SolrQuery();

        /*
        // q - 查询字符串，必须的，如果查询所有使用*:*。
        // solrParams.set("q","item_title:测试新增内容");

        //查看源码得知，相当于 solrParams.set("q","测试新增内容");
        //CommonParams中可以查看对应的前缀参数
        solrParams.setQuery("测试新增内容");

        // df-指定一个搜索Field
        solrParams.set("df","item_title");

        //fq - （filter query）过虑查询，作用：在q查询符合结果中同时是fq查询符合的
        //item_price 在 1-1000000 之间，用 * 表示无限
        //item_price:[100 TO *] 表示，item_price 大于 100
        //也可写成 solrParams.setFilterQueries("item_price:[1 TO 1000000]");
        solrParams.set("fq","item_price:[1 TO 1000000]");

        //sort - 排序
        //也可写成 solrParams.setSort("item_price", SolrQuery.ORDER.asc);
        solrParams.set("sort"," item_price desc");


        //start - 分页显示使用，开始记录下标，从0开始
        //solrParams.setStart(0);
        solrParams.set("start",0);

        //rows - 指定返回结果最多有多少条记录，配合start来实现分页。
        //solrParams.setRows(2);
        solrParams.set("rows",2);

        //fl - 指定返回那些字段内容，用逗号或空格分隔多个
        // 执行查询，只会返回 id,item_title,item_price
        //相当于 solrParams.setFields("id,item_title,item_price")；
        solrParams.set("fl","id,item_title,item_price");
         */
        // wt - 返回格式
        //高亮显示
        solrQuery.setHighlight(true);
        //设置高亮显示的域
        solrQuery.addHighlightField("description");
        //高亮显示前缀
        solrQuery.setHighlightSimplePre("<font color='red'>");
        //后缀
        solrQuery.setHighlightSimplePost("</font>");
        try {
            QueryResponse queryResponse = solrClient.query("book", solrQuery);
            if (queryResponse == null) {
                return null;
            }
            SolrDocumentList solrDocumentList = queryResponse.getResults();
            if (solrDocumentList.isEmpty()) {
                return null;
            }
            //获取高亮
            Map<String, Map<String, List<String>>> map = queryResponse.getHighlighting();
            for (SolrDocument solrDocument : solrDocumentList) {
                Book book;
                List<String> list = map.get(solrDocument.get("id")).get("description");
                if (!CollectionUtils.isEmpty(list)) {
                    solrDocument.setField("description", list.get(0));
                }
                String bookStr = JSONUtil.toJSON(solrDocument);
                book = JSON.parseObject(bookStr, Book.class);
                bookList.add(book);
            }
        } catch (SolrServerException | IOException e) {
            e.printStackTrace();
        }
        return bookList;
    }
}

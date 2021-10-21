package com.zqh.solr.domian;

import org.apache.solr.client.solrj.beans.Field;

/**
 * @description:
 * @author: zhangqinghua
 * @email: zidian.zqh@raycloud.com
 * @date: 2021/10/21 下午3:31
 */
public class Book {

    @Field
    private String id;

    @Field
    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

package com.miotech.kun.datadiscovery.model.entity;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Jie Chen
 * @created: 2020/10/20
 */
@Data
public class LineageGraph {

    List<LineageVertex> vertices = new ArrayList<>();

    List<LineageEdge> edges = new ArrayList<>();

    public void addAll(List<LineageEdge> edges) {
        this.edges.addAll(edges);
    }
}

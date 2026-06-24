package com.example.pos.dao;

import com.example.pos.models.OrderStatus;
import com.example.pos.models.SalesReportData;
import com.example.pos.models.db.OrderPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public class ReportsDao {

    @Autowired
    private MongoTemplate mongoTemplate;

    // $match - Filter date range
    // $unwind - Array becomes rows
    // $lookup - Join product collection
    // $group - aggregating like hashmap implementation
    public List<SalesReportData> getSalesReport(Instant startDate, Instant endDate, String clientName) {

        System.out.println(startDate);
        System.out.println(endDate);
        System.out.println(clientName);

        // Filter orders by date
        MatchOperation match = Aggregation.match(Criteria.where("orderTime").gte(startDate).lte(endDate));
        // Filter by order status
        MatchOperation statusMatch = Aggregation.match(Criteria.where("orderStatus").is(OrderStatus.FULFILLED));

        // unwind - Array becomes rows
        UnwindOperation unwind = Aggregation.unwind("orderItems");
        // Join product collection
        LookupOperation lookup = Aggregation.lookup("products", "orderItems.barcode",
                "barcode", "product");
        UnwindOperation unwindProduct = Aggregation.unwind("product");

        // select only required client not all clients
        MatchOperation clientMatch = Aggregation.match(Criteria.where("product.clientName").is(clientName));

        // compute - quantity * sellingPrice
        ProjectionOperation project = Aggregation.project().and("product.clientName").as("clientName")
                        .and("product.name").as("productName")
                        .and("orderItems.orderQuantity").as("quantity")
                        .andExpression("orderItems.orderQuantity * orderItems.sellingPrice").as("revenue");

        // combine same barcodes i.e. product name and client name same
        GroupOperation group = Aggregation.group("clientName", "productName")
                        .sum("quantity").as("quantity")
                        .sum("revenue").as("revenue");

        // flatten it
        ProjectionOperation finalProject = Aggregation.project()
                .and("_id.clientName").as("clientName")
                .and("_id.productName").as("productName")
                .and("quantity").as("quantity")
                .and("revenue").as("revenue");

        // Complete Pipeline
        Aggregation aggregation = Aggregation.newAggregation(match, statusMatch, unwind, lookup, unwindProduct,
                 clientMatch, project, group, finalProject);

        // Execute
        AggregationResults<SalesReportData> results =
                mongoTemplate.aggregate(aggregation, OrderPojo.class, SalesReportData.class);
        return results.getMappedResults();
    }
}

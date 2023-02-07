package com.increff.ironic.pos.model.data;

import com.increff.ironic.pos.pojo.OrderItemPojo;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrderItemChanges {

    private final List<OrderItemPojo> itemsToUpdate, itemsToDelete, itemsToAdd;
    private final List<OrderItemPojo> oldOrderItemEntities, newOrderItemEntities;

    public OrderItemChanges(List<OrderItemPojo> oldOrderItemEntities, List<OrderItemPojo> newOrderItemEntities) {
        itemsToUpdate = new LinkedList<>();
        itemsToDelete = new LinkedList<>();
        itemsToAdd = new LinkedList<>();
        this.oldOrderItemEntities = oldOrderItemEntities;
        this.newOrderItemEntities = newOrderItemEntities;
        computeChanges();
    }

    public List<OrderItemPojo> getItemsToUpdate() {
        return itemsToUpdate;
    }

    public List<OrderItemPojo> getItemsToDelete() {
        return itemsToDelete;
    }

    public List<OrderItemPojo> getItemsToAdd() {
        return itemsToAdd;
    }

    private void computeChanges() {
        Map<Integer, OrderItemPojo> oldItemMap = getItemMap(this.oldOrderItemEntities);

        for (OrderItemPojo newItem : this.newOrderItemEntities) {
            int productId = newItem.getProductId();

            if (oldItemMap.containsKey(productId)) {
                int oldItemId = oldItemMap.get(productId).getId();
                newItem.setId(oldItemId);
                itemsToUpdate.add(newItem);
                oldItemMap.remove(productId);
            } else {
                itemsToAdd.add(newItem);
            }
        }

        itemsToDelete.addAll(oldItemMap.values());
    }

    private Map<Integer, OrderItemPojo> getItemMap(List<OrderItemPojo> items) {
        Map<Integer, OrderItemPojo> itemMap = new HashMap<>();
        for (OrderItemPojo item : items) {
            itemMap.put(item.getProductId(), item);
        }
        return itemMap;
    }

    private List<Integer> getQuantity(List<OrderItemPojo> items) {
        return items.stream()
                .map(OrderItemPojo::getQuantity)
                .collect(Collectors.toList());
    }

    public List<Integer> getRequiredQuantities() {
        Map<Integer, OrderItemPojo> oldItemMap = getItemMap(this.oldOrderItemEntities);

        List<Integer> quantities = getQuantity(itemsToAdd);

        for (OrderItemPojo newItem : itemsToUpdate) {
            OrderItemPojo oldItem = oldItemMap.get(newItem.getProductId());
            int required = newItem.getQuantity() - oldItem.getQuantity();
            quantities.add(required);
        }

        List<Integer> deleteQuantities = getQuantity(itemsToDelete)
                .stream()
                .map(it -> -1 * it)
                .collect(Collectors.toList());

        quantities.addAll(deleteQuantities);
        return quantities;
    }

    public List<OrderItemPojo> getAllChanges() {
        List<OrderItemPojo> items = new LinkedList<>();
        items.addAll(itemsToAdd);
        items.addAll(itemsToUpdate);
        items.addAll(itemsToDelete);
        return items;
    }

}


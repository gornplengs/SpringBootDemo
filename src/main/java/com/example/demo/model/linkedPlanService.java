package com.example.demo.model;

public class linkedPlanService {
    private String _org;
    private String objectId;
    private String objectType;
    private linkedService linkedService;
    private planCostShares planserviceCostShares;

    public String get_org() {
        return _org;
    }

    public void set_org(String _org) {
        this._org = _org;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public com.example.demo.model.linkedService getLinkedService() {
        return linkedService;
    }

    public void setLinkedService(com.example.demo.model.linkedService linkedService) {
        this.linkedService = linkedService;
    }

    public planCostShares getPlanserviceCostShares() {
        return planserviceCostShares;
    }

    public void setPlanserviceCostShares(planCostShares planserviceCostShares) {
        this.planserviceCostShares = planserviceCostShares;
    }
}

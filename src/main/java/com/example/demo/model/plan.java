package com.example.demo.model;

import java.util.ArrayList;

public class plan {
    private String _org;
    private String creationDate;
    private String objectId;
    private String objectType;
    private String planType;
    private ArrayList<linkedPlanService> linkedPlanServices;
    private planCostShares planCostShares;

    public String get_org() {
        return _org;
    }

    public void set_org(String _org) {
        this._org = _org;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
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

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public ArrayList<linkedPlanService> getLinkedPlanServices() {
        return linkedPlanServices;
    }

    public void setLinkedPlanServices(ArrayList<linkedPlanService> linkedPlanServices) {
        this.linkedPlanServices = linkedPlanServices;
    }

    public com.example.demo.model.planCostShares getPlanCostShares() {
        return planCostShares;
    }

    public void setPlanCostShares(com.example.demo.model.planCostShares planCostShares) {
        this.planCostShares = planCostShares;
    }
}

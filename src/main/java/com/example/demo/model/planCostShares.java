package com.example.demo.model;

public class planCostShares {
    private String _org;
    private String objectId;
    private String objectType;
    private Integer deductible;
    private Integer copay;

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

    public Integer getDeductible() {
        return deductible;
    }

    public void setDeductible(Integer deductible) {
        this.deductible = deductible;
    }

    public Integer getCopay() {
        return copay;
    }

    public void setCopay(Integer copay) {
        this.copay = copay;
    }
}

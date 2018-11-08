/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.product;

import org.openvpms.archetype.rules.math.MathRules;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.math.BigDecimal;
import java.util.Date;


/**
 * Wrapper for <em>entityLink.productSupplier</em> relationships.
 *
 * @author Tim Anderson
 */
public class ProductSupplier {

    /**
     * Bean wrapper for the relationship.
     */
    private final IMObjectBean bean;


    /**
     * Constructs a {@link ProductSupplier}.
     *
     * @param relationship the relationship
     * @param service      the archetype service
     */
    public ProductSupplier(IMObjectRelationship relationship, IArchetypeService service) {
        bean = new IMObjectBean(relationship, service);
    }

    /**
     * Returns the product.
     *
     * @return the product, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Product getProduct() {
        return (Product) bean.getObject("source");
    }

    /**
     * Returns a reference to the product.
     *
     * @return the product reference, or {@code null} if none is found
     */
    public IMObjectReference getProductRef() {
        return getRelationship().getSource();
    }

    /**
     * Returns the supplier.
     *
     * @return the supplier, or {@code null} if none is found
     * @throws ArchetypeServiceException for any archetype service error
     */
    public Party getSupplier() {
        return (Party) bean.getObject("target");
    }

    /**
     * Returns a reference to the suplier.
     *
     * @return the supplier reference, or {@code null} if none is found
     */
    public IMObjectReference getSupplierRef() {
        return getRelationship().getTarget();
    }

    /**
     * Returns the active start time.
     *
     * @return the active start time. May be {@code null}
     */
    public Date getActiveStartTime() {
        return bean.getDate("activeStartTime");
    }

    /**
     * Sets the active start time.
     *
     * @param startTime the active start time
     */
    public void setActiveStartTime(Date startTime) {
        bean.setValue("activeStartTime", startTime);
    }

    /**
     * Returns the active end time.
     *
     * @return the active end time. May be {@code null}
     */
    public Date getActiveEndTime() {
        return bean.getDate("activeEndTime");
    }

    /**
     * Sets the active end time.
     *
     * @param endTime the active end time. May be {@code null}
     */
    public void setActiveEndTime(Date endTime) {
        bean.setValue("activeEndTime", endTime);
    }

    /**
     * Sets the reorder code.
     *
     * @param code the reorder code. May be {@code null}
     */
    public void setReorderCode(String code) {
        bean.setValue("reorderCode", code);
    }

    /**
     * Returns the reorder code.
     *
     * @return the reorder code. May be {@code null}
     */
    public String getReorderCode() {
        return bean.getString("reorderCode");
    }

    /**
     * Sets the reorder description.
     *
     * @param description the reorder description. May be {@code null}
     */
    public void setReorderDescription(String description) {
        bean.setValue("reorderDescription", description);
    }

    /**
     * Returns the reorder description.
     *
     * @return the reorder description. May be {@code null}
     */
    public String getReorderDescription() {
        return bean.getString("reorderDescription");
    }

    /**
     * Sets the bar code.
     *
     * @param barCode the bar code. May be {@code null}
     */
    public void setBarCode(String barCode) {
        bean.setValue("barCode", barCode);
    }

    /**
     * Returns the bar code.
     *
     * @return the bar code. May be {@code null}
     */
    public String getBarCode() {
        return bean.getString("barCode");
    }

    /**
     * Sets the package size.
     *
     * @param size the package size
     */
    public void setPackageSize(int size) {
        bean.setValue("packageSize", size);
    }

    /**
     * Returns the package size.
     *
     * @return the package size
     */
    public int getPackageSize() {
        return bean.getInt("packageSize");
    }

    /**
     * Sets the package units.
     *
     * @param units the package units. May be {@code null}
     */
    public void setPackageUnits(String units) {
        bean.setValue("packageUnits", units);
    }

    /**
     * Returns the package units.
     *
     * @return the package units. May be {@code null}
     */
    public String getPackageUnits() {
        return bean.getString("packageUnits");
    }

    /**
     * Returns the minimum order quantity.
     *
     * @return the minimum order quantity
     */
    public int getMinimumOrderQuantity() {
        return bean.getInt("minimumQty");
    }

    /**
     * Sets the minimum order quantity.
     *
     * @param quantity the minimum order quantity
     */
    public void setMinimumOrderQuantity(int quantity) {
        bean.setValue("minimumQty", quantity);
    }

    /**
     * Returns the order quantity increment.
     *
     * @return the order quantity increment
     */
    public int getOrderQuantityIncrement() {
        return bean.getInt("orderQtyInc");
    }

    /**
     * Sets the order quantity increment.
     *
     * @param increment the order quantity increment
     */
    public void setOrderQuantityIncrement(int increment) {
        bean.setValue("orderQtyInc", increment);
    }

    /**
     * Returns the lead time.
     *
     * @return the lead time
     */
    public int getLeadTime() {
        return bean.getInt("leadTime");
    }

    /**
     * Sets the lead time.
     *
     * @param leadTime the lead time
     */
    public void setLeadTime(int leadTime) {
        bean.setValue("leadTime", leadTime);
    }

    /**
     * Returns the lead time units.
     *
     * @return the lead time units. May be {@code null}
     */
    public String getLeadTimeUnits() {
        return bean.getString("leadTimeUnits");
    }

    /**
     * Sets the lead time units.
     *
     * @param units the lead time units
     */
    public void setLeadTimeUnits(String units) {
        bean.setValue("leadTimeUnits", units);
    }

    /**
     * Sets the list price.
     *
     * @param price the list price. May be {@code null}
     */
    public void setListPrice(BigDecimal price) {
        bean.setValue("listPrice", price);
    }

    /**
     * Returns the list price.
     *
     * @return the list price. May be {@code null}
     */
    public BigDecimal getListPrice() {
        return bean.getBigDecimal("listPrice");
    }

    /**
     * Sets the nett price.
     *
     * @param price the nett price. MAy be {@code null}
     */
    public void setNettPrice(BigDecimal price) {
        bean.setValue("nettPrice", price);
    }

    /**
     * Returns the nett price.
     *
     * @return the nett price
     */
    public BigDecimal getNettPrice() {
        return bean.getBigDecimal("nettPrice");
    }

    /**
     * Indicates if this is the preferred relatiobship for the supplier
     * and product.
     *
     * @param preferred if {@code true}, this is the preferred relationship
     */
    public void setPreferred(boolean preferred) {
        bean.setValue("preferred", preferred);
    }

    /**
     * Determines if this is the preferred relationship for the supplier
     * and product.
     *
     * @return {@code true} if this is the preferred relationship
     */
    public boolean isPreferred() {
        return bean.getBoolean("preferred");
    }

    /**
     * Determines if changes to the list price should trigger recalculation
     * of the <em>cost</em> and <em>price</em> nodes of any
     * <em>productPrice.unitPrice</em> associated with the product.
     *
     * @return {@code true} if unit prices should be updated
     */
    public boolean isAutoPriceUpdate() {
        return bean.getBoolean("autoPriceUpdate");
    }

    /**
     * Determines if changes to the list price should trigger recalculation
     * of the <em>cost</em> and <em>price</em> nodes of any
     * <em>productPrice.unitPrice</em> associated with the product.
     *
     * @param autoUpdate if {@code true}, unit prices should be updated
     */
    public void setAutoPriceUpdate(boolean autoUpdate) {
        bean.setValue("autoPriceUpdate", autoUpdate);
    }

    /**
     * Returns the cost price.
     * <p/>
     * This is the {@link #getListPrice() list price}/{@link #getPackageSize() package size}.
     *
     * @return the cost price, or {@code 0} if the list price is unset/0 or the package size {@code <= 0}
     */
    public BigDecimal getCostPrice() {
        BigDecimal result = BigDecimal.ZERO;
        BigDecimal listPrice = getListPrice();
        int packageSize = getPackageSize();
        if (listPrice != null && !MathRules.isZero(listPrice) && packageSize > 0) {
            result = MathRules.divide(listPrice, packageSize, 3);
        }
        return result;
    }

    /**
     * Returns the underlying relationship.
     *
     * @return the underlying relationship
     */
    public IMObjectRelationship getRelationship() {
        return (IMObjectRelationship) bean.getObject();
    }

    /**
     * Saves the underlying relationship.
     *
     * @throws ArchetypeServiceException if the object can't be saved
     */
    public void save() {
        bean.save();
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     *
     * @param other the reference object with which to compare
     * @return {@code true} if other is a {@code ProductSupplier} whose underlying {@link IMObjectRelationship} equals
     * this one.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other instanceof ProductSupplier) {
            ProductSupplier p = ((ProductSupplier) other);
            return p.bean.getObject().equals(bean.getObject());
        }
        return false;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return bean.getObject().hashCode();
    }

}

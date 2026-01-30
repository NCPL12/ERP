package com.ncpl.sales.generator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.LongType;
import org.hibernate.type.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import com.ncpl.sales.model.PurchaseItem;
import com.ncpl.sales.model.PurchaseOrder;
import com.ncpl.sales.model.SalesItem;
import com.ncpl.sales.service.SalesService;

@Component
public class PurchaseOrderIdGenerator extends SequenceStyleGenerator {
	public static ApplicationContext ctx;
	@Autowired
    private void setApplicationContext(ApplicationContext applicationContext) {
        ctx = applicationContext;       
    }
	
	public static final String VALUE_PREFIX_PARAMETER = "valuePrefix";
    public static final String VALUE_PREFIX_DEFAULT = "";
    //private String valuePrefix;
 
    public static final String NUMBER_FORMAT_PARAMETER = "numberFormat";
    public static final String NUMBER_FORMAT_DEFAULT = "%d";
    private String numberFormat;
    
	@Override
    public Serializable generate(SharedSessionContractImplementor session,
            Object object) throws HibernateException {
    	
    	SalesService salesService = ctx.getBean(SalesService.class);
    	
    	List<String> abbrList = new ArrayList<String>();
    	List<String> regionList = new ArrayList<String>();
    	String PO_NUM_CONST = "";
    	//String PO_NUM_CONST = "BGLR";
    	StringBuilder poNumberPrefix = new StringBuilder();
    	//poNumberPrefix.append(PO_NUM_CONST);
    	//poNumberPrefix.append("-");
    	
    	PurchaseOrder purchaseOrder = (PurchaseOrder) object;
    	List<PurchaseItem> purchaseItems = purchaseOrder.getItems();
    	for (PurchaseItem purchaseItem : purchaseItems) {
    		
    		String salesItemId = purchaseItem.getDescription();
        	boolean value = false;
    		Optional<SalesItem> soitem = salesService.getSalesItemById(salesItemId,value);
        	String partyAbbrivation = soitem.get().getSalesOrder().getParty().getAbbrivation();
        	String region = soitem.get().getSalesOrder().getRegion();
        	
        	if(partyAbbrivation == null || partyAbbrivation.equals("")) {
        		partyAbbrivation = soitem.get().getSalesOrder().getParty().getPartyName().substring(0, 2).trim();
        	}
        	
        	if(abbrList.size() > 0 && abbrList.contains(partyAbbrivation)) {
        		continue;
        	}
        	poNumberPrefix.append(partyAbbrivation);
        	poNumberPrefix.append("-");
        	abbrList.add(partyAbbrivation);
        	regionList.add(region);
        	
		}
    	if(regionList.size()>0) {
	    	if(regionList.contains("Mangalore") && regionList.contains("Bangalore")) {
	    		PO_NUM_CONST= "BGLR-MGLR";
	    	}else if(regionList.contains("Mangalore") && !regionList.contains("Bangalore")) {
	    		PO_NUM_CONST= "MGLR";
	    	}else {
	    		PO_NUM_CONST= "BGLR";
	    	}
    	}else {
    		PO_NUM_CONST= "BGLR";
    	}
    	
    	int year = Calendar.getInstance().get(Calendar.YEAR);
    	String ponumber = PO_NUM_CONST + "-" + poNumberPrefix + String.format(numberFormat, super.generate(session, object))+"-"+year;
        System.out.println(ponumber);
        return ponumber;
    }
 
    @Override
    public void configure(Type type, Properties params,
            ServiceRegistry serviceRegistry) throws MappingException {
        super.configure(LongType.INSTANCE, params, serviceRegistry);
		
       // String valuePrefix = "PO";
        numberFormat = ConfigurationHelper.getString(NUMBER_FORMAT_PARAMETER,
                params, NUMBER_FORMAT_DEFAULT);
    }
}


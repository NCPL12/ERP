package com.ncpl.sales.generator;

import java.io.Serializable;
import java.util.Calendar;
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

import com.ncpl.sales.model.Invoice;
import com.ncpl.sales.model.SalesOrder;
import com.ncpl.sales.service.SalesService;

@Component
public class InvoiceIdGenerator extends SequenceStyleGenerator {
	
	public static ApplicationContext ctx;
	@Autowired
    private void setApplicationContext(ApplicationContext applicationContext) {
        ctx = applicationContext;       
    }
	
	public static final String VALUE_PREFIX_PARAMETER = "valuePrefix";
    public static final String VALUE_PREFIX_DEFAULT = "";
    private String valuePrefix;
 
    public static final String NUMBER_FORMAT_PARAMETER = "numberFormat";
    public static final String NUMBER_FORMAT_DEFAULT = "%d";
    private String numberFormat;
    
    @Override
    public Serializable generate(SharedSessionContractImplementor session,
            Object object) throws HibernateException {
    	
    	SalesService salesService = ctx.getBean(SalesService.class);
    	
    	Invoice invObj = (Invoice) object;
    	
    	Optional<SalesOrder> soObj = salesService.getSalesOrderById(invObj.getSoNumber());
    	String partyAbbrivation = soObj.get().getParty().getAbbrivation();
    	
    	if(partyAbbrivation == null || partyAbbrivation.equals("")) {
    		partyAbbrivation = soObj.get().getParty().getPartyName().substring(0, 2);
    	}
    	
    	
    	String region = soObj.get().getRegion();
    	if(region == null){
    		region = "NA";
    	}
        
    	if(region.equalsIgnoreCase("bangalore")){
    		region = "BGLR";
    	}else{
    		region = "MGLR";
    	}
    	/*
    	 * For appending the city and abbreviation to id casted object to sales order..
    	 * returning the purchaseOrder id with appended values..
    	 */
    	//String city = "BLR";
    	//String abbrivation = "ELT";

    	int year = Calendar.getInstance().get(Calendar.YEAR);

    	String s = valuePrefix + "-" + region + "-" +partyAbbrivation+"-"+ String.format(numberFormat, super.generate(session, object)) +"-" +year;

        System.out.println(s);
        return s;
    }
 
    @Override
    public void configure(Type type, Properties params,
            ServiceRegistry serviceRegistry) throws MappingException {
        super.configure(LongType.INSTANCE, params, serviceRegistry);
		/*
		 * valuePrefix = ConfigurationHelper.getString(VALUE_PREFIX_PARAMETER, params,
		 * VALUE_PREFIX_DEFAULT);
		 */
        
        // Defined user oriented prefix
        
        valuePrefix = "INV";
        numberFormat = ConfigurationHelper.getString(NUMBER_FORMAT_PARAMETER,
                params, NUMBER_FORMAT_DEFAULT);
    }

}

package com.ncpl.sales.generator;

import java.io.Serializable;
import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.LongType;
import org.hibernate.type.Type;

public class StockIdGenerator extends SequenceStyleGenerator{
	public static final String VALUE_PREFIX_PARAMETER = "valuePrefix";
    public static final String VALUE_PREFIX_DEFAULT = "";
    private String valuePrefix;
 
    public static final String NUMBER_FORMAT_PARAMETER = "numberFormat";
    public static final String NUMBER_FORMAT_DEFAULT = "%d";
    private String numberFormat;
    
    @Override
    public Serializable generate(SharedSessionContractImplementor session,
            Object object) throws HibernateException {
    	/*
    	 * For appending the city and abbreviation to id casted object to sales order..
    	 * returning the purchaseOrder id with appended values..
    	 */
    	//ItemMaster itemMaster = (ItemMaster) object;
    	String city = "BLR";
    	String abbrivation = "ELT";
    	String s = valuePrefix + "-" + city + "-" + abbrivation + "-" + String.format(numberFormat, super.generate(session, object)) +"-" +"2020";
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
        
        valuePrefix = "ST";
        numberFormat = ConfigurationHelper.getString(NUMBER_FORMAT_PARAMETER,
                params, NUMBER_FORMAT_DEFAULT);
    }

}

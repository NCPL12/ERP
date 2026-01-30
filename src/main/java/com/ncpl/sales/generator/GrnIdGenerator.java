package com.ncpl.sales.generator;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import com.ncpl.sales.model.Grn;

import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.enhanced.SequenceStyleGenerator;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.type.LongType;
import org.hibernate.type.Type;

public class GrnIdGenerator extends SequenceStyleGenerator{
    public static final String VALUE_PREFIX_PARAMETER = "valuePrefix";
    public static final String VALUE_PREFIX_DEFAULT = "";
    private String valuePrefix;
 
    public static final String NUMBER_FORMAT_PARAMETER = "numberFormat";
    public static final String NUMBER_FORMAT_DEFAULT = "%03d";
    private String numberFormat;
    
    @SuppressWarnings("unused")
	@Override
    public Serializable generate(SharedSessionContractImplementor session,
            Object object) throws HibernateException {
    	/*
    	 * For appending the city and abbreviation to id casted object to sales order..
    	 * returning the purchaseOrder id with appended values..
    	 */
        Grn grn = (Grn) object;
        DateFormat df = new SimpleDateFormat("dd/MM/yy");
        Date dateobj = new Date();
        String date = df.format(dateobj);
        date =date.replace("/","");
    	String s = valuePrefix +" " + String.format(numberFormat, super.generate(session, object)) +"-" +date;
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
        
        valuePrefix = "GRN";
        numberFormat = ConfigurationHelper.getString(NUMBER_FORMAT_PARAMETER,
                params, NUMBER_FORMAT_DEFAULT);
    }
}
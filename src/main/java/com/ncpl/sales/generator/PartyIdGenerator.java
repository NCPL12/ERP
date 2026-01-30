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
import org.springframework.beans.factory.annotation.Autowired;

import com.ncpl.sales.model.City;
import com.ncpl.sales.model.Party;
import com.ncpl.sales.repository.PartyRepo;

public class PartyIdGenerator extends SequenceStyleGenerator{
	
	@Autowired
	PartyRepo partyrepo;
	
	public static final String VALUE_PREFIX_PARAMETER = "valuePrefix";
    public static final String VALUE_PREFIX_DEFAULT = "";
    private String valuePrefix;
 
    public static final String NUMBER_FORMAT_PARAMETER = "numberFormat";
    public static final String NUMBER_FORMAT_DEFAULT = "%d";
    private String numberFormat;
    @Override
    public Serializable generate(SharedSessionContractImplementor session,
            Object object) throws HibernateException {
    	
       Party p = (Party) object;
       
       String abbrevation = p.getAbbrivation();
       City cityObj = p.getParty_city();
       String city = cityObj.getName();
       String regex="[^a-zA-Z]";
       city=city.replaceAll(regex, "");
       city=city.substring(0, 3);
       
    	String s = valuePrefix + "-"  + city + "-" + abbrevation + "-" +String.format(numberFormat, super.generate(session, object)) + "/2019"; 
        return s;
    }
 
    @Override
    public void configure(Type type, Properties params,
            ServiceRegistry serviceRegistry) throws MappingException {
        super.configure(LongType.INSTANCE, params, serviceRegistry);
        valuePrefix = "PA";
        numberFormat = ConfigurationHelper.getString(NUMBER_FORMAT_PARAMETER,
                params, NUMBER_FORMAT_DEFAULT);
       // System.out.println(numberFormat);
      
       }
    
   
}

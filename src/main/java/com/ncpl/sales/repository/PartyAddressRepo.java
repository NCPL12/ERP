package com.ncpl.sales.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ncpl.sales.model.PartyAddress;

public interface PartyAddressRepo extends JpaRepository<PartyAddress, String> {
	@Query("select pa.id, pa.addr1 from PartyAddress pa where pa.id in :ids")
	List<Object[]> findDcShippingAddresses(@Param("ids") List<String> ids);
	@Query(" from PartyAddress where party_id=?1 ")
	List<PartyAddress> getAddressByPartyId(String partyId);

	@Query("select count(*) from PartyAddress where party_id=?1 ")
	int getNoofAddressByPartyId(String partyId);


}

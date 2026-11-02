package com.ncpl.sales.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.ncpl.sales.controller.ReturnableDcController.ReturnableDc;

@Service
public class ReturnableDcService {

	public List<ReturnableDc> getAll() {
		// TODO: replace the SAMPLE rows below with your DAO/repository call,
		// e.g. return returnableDcDao.findAll();
		List<ReturnableDc> list = new ArrayList<>();
		list.add(row(1L, "RDC-0001", "9264", "TS25M052 - IISc Medical School Foun...", "03 Third Floor, Campus 10, Wing A ...", "17-8-2026 08:20:10"));
		list.add(row(2L, "RDC-0002", "9262", "Nhance Digital Buildtech Private Li...", "SEI Technology Services India Pvt L...", "17-8-2026 08:02:44"));
		list.add(row(3L, "RDC-0003", "9261", "Devas Global Services LLP", "Purva Aerocity - Block B & Purva Ae...", "17-8-2026 07:49:56"));
		return list;
	}

	private ReturnableDc row(Long id, String rdcNo, String dcNo, String client, String address, String date) {
		ReturnableDc r = new ReturnableDc();
		r.setId(id);
		r.setReturnableDcNo(rdcNo);
		r.setDcNo(dcNo);
		r.setClientName(client);
		r.setShippingAddress(address);
		r.setDate(date);
		return r;
	}
}

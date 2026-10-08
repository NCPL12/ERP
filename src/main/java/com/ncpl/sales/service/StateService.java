package com.ncpl.sales.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ncpl.sales.model.State;

import java.util.Collections;
import java.util.List;
import com.ncpl.sales.repository.StateRepo;
@Service
public class StateService {
	@Autowired
	StateRepo stateRepo;
	
	public boolean saveState(State state){
		stateRepo.save(state);
		return true;
	}
	public Optional<State> findSateById(Long id) {
		Optional<State> state = null;
		if(id!=null) {
			state = stateRepo.findById(id);
		}
		return state;
	}

	public List<State> stateList(){
		List<State> stateList = stateRepo.findAll();
		Collections.sort(stateList);
		return stateList;
	}

}

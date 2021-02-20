package com.hissummer.mockserver.mgmt.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.hissummer.mockserver.mgmt.entity.HttpMockRule;
import com.hissummer.mockserver.mgmt.entity.RuleCategory;
import com.hissummer.mockserver.mgmt.exception.ServiceException;
import com.hissummer.mockserver.mgmt.service.jpa.HttpMockRuleMongoRepository;
import com.hissummer.mockserver.mgmt.service.jpa.RuleCategoryMongoRepository;

@Service
public class RuleCategoryServiceImpl {

	@Autowired
	RuleCategoryMongoRepository ruleCategoryMongoRepository;

	@Autowired
	HttpMockRuleMongoRepository httpMockRuleMongoRepository;

	@Transactional
	public RuleCategory addCategory(RuleCategory category) throws ServiceException {

		if (StringUtils.isEmpty(category.getCategory())) {
			throw ServiceException.builder().status(0).serviceMessage("Category is empty").build();

		}
		RuleCategory foundCategory = ruleCategoryMongoRepository.findByCategory(category.getCategory());

		if (foundCategory == null) {

			return ruleCategoryMongoRepository.insert(category);
		} else {

			return foundCategory;

		}

	}

	@Transactional
	public RuleCategory updateCategory(RuleCategory category) throws ServiceException {

		if (StringUtils.isEmpty(category.getId())) {
			throw ServiceException.builder().status(0).serviceMessage("Id is empty").build();

		}
		
		RuleCategory foundCategoryByName = ruleCategoryMongoRepository.findByCategory(category.getCategory());
		
		if(foundCategoryByName != null)
		{
			throw ServiceException.builder().status(0).serviceMessage("Already exists the category name.").build();
			
		}

		Optional<RuleCategory> foundCategory = ruleCategoryMongoRepository.findById(category.getId());

		if (!foundCategory.isPresent()) {

			throw ServiceException.builder().status(0).serviceMessage("Can not find the category.").build();
		} else {

			List<HttpMockRule> httpmockrules =  httpMockRuleMongoRepository.findByCategory(foundCategory.get().getCategory());
			
			httpmockrules.forEach(rule->{
				rule.setCategory(category.getCategory());
				
			});
			httpMockRuleMongoRepository.saveAll(httpmockrules);
			return ruleCategoryMongoRepository.save(category);
		}

	}

	@Transactional
	public RuleCategory deleteCategory(RuleCategory category) throws ServiceException {

		if (StringUtils.isEmpty(category.getId())) {
			throw ServiceException.builder().status(0).serviceMessage("Id is empty").build();

		}

		Optional<RuleCategory> foundCategory = ruleCategoryMongoRepository.findById(category.getId());

		if (!foundCategory.isPresent()) {

			throw ServiceException.builder().status(0).serviceMessage("Can not find the category.").build();
		} else {
			Long count = httpMockRuleMongoRepository.countByCategory(foundCategory.get().getCategory());

			if (count > 0) {
				throw ServiceException.builder().status(0).serviceMessage("Still exist rules under this category.")
						.build();

			}
			ruleCategoryMongoRepository.delete(category);
			return category;

		}

	}

	public RuleCategory findByCategory(String category) throws ServiceException {

		if (StringUtils.isEmpty(category)) {
			throw ServiceException.builder().status(0).serviceMessage("category naame is empty").build();

		}

		RuleCategory foundCategory = ruleCategoryMongoRepository.findByCategory(category);

		if (foundCategory == null) {

			throw ServiceException.builder().status(0).serviceMessage("Can not find the category.").build();
		} else {
			return foundCategory;
		}

	}

	public Page<RuleCategory> queryCategories(int pageSize, int pageNumber) {

		PageRequest page = PageRequest.of(pageNumber, pageSize);

		Page<RuleCategory> categories = ruleCategoryMongoRepository.findAll(page);
		if (categories != null && !categories.getContent().isEmpty())
			return categories;
		else
			return Page.empty();

	}

	public List<RuleCategory> queryCategories() {

		List<RuleCategory> categories = ruleCategoryMongoRepository.findAll();
		return categories;

	}

}

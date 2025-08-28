/*
 * Copyright 2025-2025 Chris de Vreeze
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.cdevreeze.todo.service.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import eu.cdevreeze.todo.entity.AddressEntity;
import eu.cdevreeze.todo.model.Address;
import eu.cdevreeze.todo.service.AddressService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaDelete;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Default AddressService implementation.
 *
 * @author Chris de Vreeze
 */
@Service
public class DefaultAddressService implements AddressService {

    // See https://thorben-janssen.com/hibernate-tips-how-to-bootstrap-hibernate-with-spring-boot/

    private final EntityManager entityManager;

    public DefaultAddressService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    @Transactional(readOnly = true)
    public ImmutableList<Address> findAllAddresses() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<AddressEntity> cq = cb.createQuery(AddressEntity.class);

        Root<AddressEntity> addressRoot = cq.from(AddressEntity.class);
        cq.select(addressRoot);

        return entityManager.createQuery(cq)
                .getResultStream()
                .map(AddressEntity::toModel)
                .collect(ImmutableList.toImmutableList());
    }

    @Override
    @Transactional
    public Address addAddress(Address address) {
        Preconditions.checkArgument(address.idOption().isEmpty());
        AddressEntity addressEntity = AddressEntity.fromModel(address);

        entityManager.persist(addressEntity);
        entityManager.flush();

        var resultAddress = addressEntity.toModel();
        Preconditions.checkArgument(resultAddress.idOption().isPresent());
        return resultAddress;
    }

    @Override
    @Transactional
    public void deleteAllAddresses() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaDelete<AddressEntity> cd = cb.createCriteriaDelete(AddressEntity.class);

        entityManager.createQuery(cd).executeUpdate();
    }
}

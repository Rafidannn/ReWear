package com.example.application.service.user;

import com.example.application.model.user.Address;
import com.example.application.model.user.User;
import com.example.application.repository.user.AddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AddressService {

    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public List<Address> getAddressesByUser(User user) {
        return addressRepository.findByUserAndDeletedAtIsNull(user);
    }

    public Optional<Address> getPrimaryAddress(User user) {
        return addressRepository.findByUserAndIsPrimaryTrueAndDeletedAtIsNull(user);
    }

    @Transactional
    public Address saveAddress(Address address) {
        // Jika ini primary, lepas primary dari yang lain
        if (address.isPrimary()) {
            List<Address> existing = addressRepository.findByUserAndDeletedAtIsNull(address.getUser());
            for (Address a : existing) {
                if (a.isPrimary() && !a.getId().equals(address.getId())) {
                    a.setPrimary(false);
                    addressRepository.save(a);
                }
            }
        }
        return addressRepository.save(address);
    }

    @Transactional
    public void setPrimary(Long addressId, User user) {
        List<Address> all = addressRepository.findByUserAndDeletedAtIsNull(user);
        for (Address a : all) {
            a.setPrimary(a.getId().equals(addressId));
            addressRepository.save(a);
        }
    }

    @Transactional
    public void deleteAddress(Long addressId) {
        addressRepository.findById(addressId).ifPresent(a -> {
            a.setDeletedAt(java.time.LocalDateTime.now());
            addressRepository.save(a);
        });
    }
}

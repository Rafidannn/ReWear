package com.example.application.service.user;

import com.example.application.model.user.*;
import com.example.application.repository.user.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final UserSchoolVerificationRepository verificationRepository;
    private final BankAccountRepository bankAccountRepository;

    public UserService(UserRepository userRepository,
                       AddressRepository addressRepository,
                       UserSchoolVerificationRepository verificationRepository,
                       BankAccountRepository bankAccountRepository) {
        this.userRepository = userRepository;
        this.addressRepository = addressRepository;
        this.verificationRepository = verificationRepository;
        this.bankAccountRepository = bankAccountRepository;
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public List<Address> getUserAddresses(User user) {
        return addressRepository.findByUserAndDeletedAtIsNull(user);
    }

    public Address saveAddress(Address address) {
        return addressRepository.save(address);
    }

    public Optional<UserSchoolVerification> getVerification(User user) {
        return verificationRepository.findByUser(user);
    }

    public UserSchoolVerification requestSchoolVerification(UserSchoolVerification verification) {
        verification.setStatus(VerificationStatus.PENDING);
        return verificationRepository.save(verification);
    }

    public List<BankAccount> getUserBankAccounts(User user) {
        return bankAccountRepository.findByUser(user);
    }

    public BankAccount saveBankAccount(BankAccount bankAccount) {
        return bankAccountRepository.save(bankAccount);
    }
}

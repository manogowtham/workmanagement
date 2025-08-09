package com.banyan.workmanagement.model;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.*;

@Entity
public class WorkDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String engineerName;
    private String customerName;

    private String location;
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private LocalDate date;
    private LocalTime inTime;
    private LocalTime outTime;

    private String status;
    private String description;
    private String typeOfService;

    @Column(length = 1000)
    private String workDescription;

    private Double billAmount;
    private String receivePayment;

    @Column(length = 1000)
    private String bankDetails;

    private Integer kilometer;

    private String paymentMode; // Optional: assuming you meant to add this too
    private String typeOfWork; // Newly added field

    // Constructors
    public WorkDetails() {
    }

    public WorkDetails(Long id, String engineerName, String customerName, String location, LocalDate date,
            LocalTime inTime, LocalTime outTime, String workType, String status, String description,
            String workDescription, Double billAmount, String receivePayment, String bankDetails,
            Integer kilometer, String paymentMode, String typeOfWork) {
        this.id = id;
        this.engineerName = engineerName;
        this.customerName = customerName;
        this.location = location;
        this.date = date;
        this.inTime = inTime;
        this.outTime = outTime;

        this.status = status;
        this.description = description;
        this.workDescription = workDescription;
        this.billAmount = billAmount;
        this.receivePayment = receivePayment;
        this.bankDetails = bankDetails;
        this.kilometer = kilometer;
        this.paymentMode = paymentMode;
        this.typeOfWork = typeOfWork;
    }

    public WorkDetails(String engineerName, String customerName, String location, LocalDate date,
            LocalTime inTime, LocalTime outTime, String workType, String status, String description,
            String workDescription, Double billAmount, String receivePayment, String bankDetails,
            Integer kilometer, String paymentMode, String typeOfWork) {
        this.engineerName = engineerName;
        this.customerName = customerName;
        this.location = location;
        this.date = date;
        this.inTime = inTime;
        this.outTime = outTime;

        this.status = status;
        this.description = description;
        this.workDescription = workDescription;
        this.billAmount = billAmount;
        this.receivePayment = receivePayment;
        this.bankDetails = bankDetails;
        this.kilometer = kilometer;
        this.paymentMode = paymentMode;
        this.typeOfWork = typeOfWork;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEngineerName() {
        return engineerName;
    }

    public void setEngineerName(String engineerName) {
        this.engineerName = engineerName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getInTime() {
        return inTime;
    }

    public void setInTime(LocalTime inTime) {
        this.inTime = inTime;
    }

    public LocalTime getOutTime() {
        return outTime;
    }

    public void setOutTime(LocalTime outTime) {
        this.outTime = outTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWorkDescription() {
        return workDescription;
    }

    public void setWorkDescription(String workDescription) {
        this.workDescription = workDescription;
    }

    public Double getBillAmount() {
        return billAmount;
    }

    public void setBillAmount(Double billAmount) {
        this.billAmount = billAmount;
    }

    public String getReceivePayment() {
        return receivePayment;
    }

    public void setReceivePayment(String receivePayment) {
        this.receivePayment = receivePayment;
    }

    public String getBankDetails() {
        return bankDetails;
    }

    public void setBankDetails(String bankDetails) {
        this.bankDetails = bankDetails;
    }

    public Integer getKilometer() {
        return kilometer;
    }

    public void setKilometer(Integer kilometer) {
        this.kilometer = kilometer;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public void setPaymentMode(String paymentMode) {
        this.paymentMode = paymentMode;
    }

    public String getTypeOfWork() {
        return typeOfWork;
    }

    public void setTypeOfWork(String typeOfWork) {
        this.typeOfWork = typeOfWork;
    }

    public String getTypeOfService() {
        return typeOfService;
    }

    // Setter
    public void setTypeOfService(String typeOfService) {
        this.typeOfService = typeOfService;
    }

    @Override
    public String toString() {
        return "WorkDetails{" +
                "id=" + id +
                ", engineerName='" + engineerName + '\'' +
                ", customerName='" + customerName + '\'' +
                ", location='" + location + '\'' +
                ", date='" + date + '\'' +
                ", inTime='" + inTime + '\'' +
                ", outTime='" + outTime + '\'' +

                ", status='" + status + '\'' +

                ", workDescription='" + workDescription + '\'' +
                ", billAmount=" + billAmount +
                ", receivePayment='" + receivePayment + '\'' +
                ", bankDetails='" + bankDetails + '\'' +
                ", kilometer=" + kilometer +
                ", paymentMode='" + paymentMode + '\'' +
                ", typeOfWork='" + typeOfWork + '\'' +
                '}';
    }

}

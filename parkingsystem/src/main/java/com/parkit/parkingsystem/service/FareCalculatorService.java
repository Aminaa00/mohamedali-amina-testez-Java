package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket, boolean discount){
        if( (ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime())) ){
            throw new IllegalArgumentException("Out time provided is incorrect:"+ticket.getOutTime().toString());
        }

        long inTime = ticket.getInTime().getTime();
        long outTime = ticket.getOutTime().getTime();

        long duration = outTime - inTime;
        double durationInTime = (double) (duration) / (1000 * 60 * 60);

        if (durationInTime < 0.5) {
            ticket.setPrice(0); 
        } else {
            switch (ticket.getParkingSpot().getParkingType()){
                case CAR:
                if (discount) {
                    ticket.setPrice(durationInTime * Fare.CAR_RATE_PER_HOUR * 0.95); 
                } else { 
                    ticket.setPrice(durationInTime * Fare.CAR_RATE_PER_HOUR); 
                } 
                break; 
 
                case BIKE:
                if (discount) { 
                    ticket.setPrice(durationInTime * Fare.BIKE_RATE_PER_HOUR * 0.95); 
                } else { 
                    ticket.setPrice(durationInTime * Fare.BIKE_RATE_PER_HOUR); 
                } 
                break; 
 
            default: 
                throw new IllegalArgumentException("Unknown Parking Type");

            }
        }
    }
    public void calculateFare(Ticket ticket) { 
        calculateFare(ticket, false); 
    }
}
package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import com.parkit.parkingsystem.constants.Fare;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.util.Date;

import org.junit.jupiter.api.Assertions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ParkingServiceTest {

    private static ParkingService parkingService;

    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;


    @BeforeEach
    void setUpPerTest() {
        try {
            MockitoAnnotations.openMocks(this);

            ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
            Ticket ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber("ABCDEF");

            lenient().when(inputReaderUtil.readSelection()).thenReturn(1); // I select a CAR
            lenient().when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
            lenient().when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF"); // Ajout de cette ligne
            lenient().when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);

            parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @Test
    public void processExitingVehicleTest(){
        Date todayDateMinusOneHour = new Date(System.currentTimeMillis() - (60 * 60 * 1000));
        Ticket ticket = new Ticket();
        ticket.setVehicleRegNumber("ABCDEF");
        ticket.setInTime(todayDateMinusOneHour);
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR,false);
        ticket.setParkingSpot(parkingSpot);
        ticketDAO.saveTicket(ticket);

        lenient().when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        lenient().when(ticketDAO.getNbTicket("ABCDEF")).thenReturn(1);
        lenient().when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        parkingService.processExitingVehicle();

        Ticket ticketResult = ticketDAO.getTicket("ABCDEF");
        assertEquals(ticketResult.getPrice(), Fare.CAR_RATE_PER_HOUR , 0.01);
        verify(ticketDAO, times(1)).updateTicket(any(Ticket.class));
        verify(ticketDAO, times(1)).getNbTicket(anyString());
    }


    @Test
    public void testProcessIncomingVehicle() throws Exception {
        parkingService.processIncomingVehicle();

        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        verify(parkingSpotDAO, Mockito.times(1)).getNextAvailableSlot(any(ParkingType.class));
        verify(ticketDAO, Mockito.times(1)).saveTicket(any(Ticket.class));
    }

    @Test
    public void processExitingVehicleTestUnableUpdate() throws Exception {

        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        Ticket ticket = new Ticket();
        ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDEF");
        
        when(ticketDAO.getTicket(anyString())).thenReturn(ticket);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);

        parkingService.processExitingVehicle();
        
        verify(parkingSpotDAO, Mockito.times(0)).updateParking(any(ParkingSpot.class));
        verify(ticketDAO, times(1)).updateTicket(any(Ticket.class));
        verify(ticketDAO, times(1)).getNbTicket(anyString());
    }


//3.testGetNextParkingNumberIfAvailable : test de l’appel de la méthode getNextParkingNumberIfAvailable()
//avec pour résultat l’obtention d’un spot dont l’ID est 1 et qui est disponible.

    @Test
    public void testGetNextParkingNumberIfAvailable() {
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(1);

        ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();

        assertEquals(parkingSpot.getId(), result.getId());
        assertTrue(result.isAvailable());
        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(any(ParkingType.class));
    }

    
    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() {
        when(parkingSpotDAO.getNextAvailableSlot(any(ParkingType.class))).thenReturn(0);
    
        ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();
    
        Assertions.assertNull(result);
        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(any(ParkingType.class));
        verify(parkingSpotDAO, times(0)).updateParking(any(ParkingSpot.class));
    }


    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberWrongArgument() {
        when(inputReaderUtil.readSelection()).thenReturn(3);

        ParkingSpot result = parkingService.getNextParkingNumberIfAvailable();

        Assertions.assertNull(result);
        verify(inputReaderUtil, times(1)).readSelection();
        verify(parkingSpotDAO, times(0)).getNextAvailableSlot(any(ParkingType.class));
        verify(parkingSpotDAO, times(0)).updateParking(any(ParkingSpot.class));
    }




}



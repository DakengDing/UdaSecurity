package com.udacity.catpoint.security.service;

import com.udacity.catpoint.image.service.AwsImageService;
import com.udacity.catpoint.image.service.FakeImageService;
import com.udacity.catpoint.image.service.ImageServiceInterface;
import com.udacity.catpoint.security.application.StatusListener;
import com.udacity.catpoint.security.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

class SecurityServiceTest {

            SecurityService securityService;
    @Mock
            SecurityRepository securityRepository;
    @Mock
            ImageServiceInterface imageService;
    @Mock
            StatusListener statusListener;


    private Sensor testSensor;
    private String randomUUID= UUID.randomUUID().toString();

    private Set<Sensor> getAllSensors (int counter,boolean status){
        Set<Sensor>sensorSet = new HashSet<>();
        for(int i=0;i<counter;i++){
            sensorSet.add(new Sensor(randomUUID, SensorType.DOOR));
        }
        sensorSet.forEach(sensor->sensor.setActive(status));
        return sensorSet;
    }

    private Sensor getSensor(){
        return new Sensor(randomUUID,SensorType.DOOR);
    }

    @BeforeEach
    void init(){
        securityService = new SecurityService(securityRepository,imageService);
        testSensor = getSensor();
    }

    @Test//1
    public void put_the_system_into_pending_alarm_status() {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        securityService.changeSensorActivationStatus(testSensor,true);
        verify(securityRepository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    @Test
    //test2
    public void set_the_alarm_status_to_alarm() {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(testSensor,true);
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    //test3
    public void return_to_no_alarm_state() {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(testSensor);
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    //test4
    public void change_in_sensor_state_should_not_affect_the_alarm_state(boolean status) {
        if (status){
            when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        }
        securityService.changeSensorActivationStatus(testSensor, status);

        verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));
    }

    @Test
    //test5
    public void change_sensor_state_to_alarm_state() {
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

        securityService.changeSensorActivationStatus(testSensor,true);
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @ParameterizedTest
    @EnumSource(value = AlarmStatus.class, names = {"NO_ALARM", "PENDING_ALARM", "ALARM"})
    //test6
    public void make_no_changes_to_the_alarm_state(AlarmStatus status) {

        securityService.changeSensorActivationStatus(testSensor,false);
        verify(securityRepository,never()).setAlarmStatus(any(AlarmStatus.class));
    }

    @Test
    //test7
    public void cat_put_the_system_into_alarm_status() {
        when(imageService.imageContainsCat(any(),anyFloat())).thenReturn(true);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        securityService.processImage(mock(BufferedImage.class));
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    //test8
    public void not_cat_change_the_status_to_no_alarm_as_long_as_the_sensors_are_not_active() {
        securityService.changeSensorActivationStatus(testSensor);
        when(imageService.imageContainsCat(any(),anyFloat())).thenReturn(false);

        securityService.processImage(mock(BufferedImage.class));
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @Test
    //test9
    public void disarmed_set_the_status_to_no_alarm() {
        securityService.setArmingStatus(ArmingStatus.DISARMED);
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_HOME", "ARMED_AWAY"})
    //test10
    public void armed_reset_all_sensors_to_inactive() {
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        assertTrue(securityService.getSensors().stream().allMatch(sensor -> Boolean.FALSE.equals(sensor.getActive())));
    }

    @Test
    //test11
    public void armed_cat_reset_all_sensors_to_active(){
        BufferedImage catImage = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        when(imageService.imageContainsCat(any(),anyFloat())).thenReturn(true);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        securityService.processImage(catImage);
        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    @Test
    public void statusListenerTest(){
        securityService.addStatusListener(statusListener);
        securityService.removeStatusListener(statusListener);
    }

    @Test
    public void sensorTest(){
        securityService.addSensor(testSensor);
        securityService.removeSensor(testSensor);
    }
    @Test
    public void changeSensorActivationStatus(){
        Mockito.when(securityService.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(testSensor,false);
        securityService.changeSensorActivationStatus(testSensor,true);
        Mockito.when(securityService.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        securityService.changeSensorActivationStatus(testSensor,false);
        securityService.changeSensorActivationStatus(testSensor,true);
    }







}
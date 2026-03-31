package com.seatbooking.booking_service.aop;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class MetricsAspect {

    private final MeterRegistry meterRegistry;
    private final Tracer tracer;

    @Around("@annotation(com.seatbooking.booking_service.aop.TrackMetrics)")
    public Object trackBusinessMetrics(ProceedingJoinPoint joinPoint) throws Throwable{
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        TrackMetrics annotation = method.getAnnotation(TrackMetrics.class);

        String operation = annotation.operation();
        boolean trackTime = annotation.trackTime();
        boolean trackCount = annotation.trackCount();

        Timer.Sample timerSample = null;
        if(trackTime){
            timerSample = Timer.start(meterRegistry);
        }
        try{
            Object result = joinPoint.proceed();

            if(trackCount){
                Counter.builder("business.operation.success")
                        .tag("operation", operation)
                        .description("Successful business operations")
                        .register(meterRegistry)
                        .increment();
            }

            if(trackTime && timerSample != null){
                timerSample.stop(Timer.builder("business.operation.duration")
                        .tag("operation", operation)
                        .tag("status","success")
                        .description("business operation execution time")
                        .register(meterRegistry));
            }
            return result;
        } catch (Throwable e){
            if(trackCount){
                Counter.builder("business.operation.failure")
                        .tag("operation", operation)
                        .tag("exception",e.getClass().getSimpleName())
                        .description("Failed business operations")
                        .register(meterRegistry)
                        .increment();
            }

            if(trackTime && timerSample!= null){
                timerSample.stop(Timer.builder("business.operation.duration")
                        .tag("operation",operation)
                        .tag("status", "failure")
                        .description("business operation execution time")
                        .register(meterRegistry));
            }
            throw e;
        }
    }

    @Around("@annotation(com.seatbooking.booking_service.aop.TrackDBTime)")
    public Object trackDBTime(ProceedingJoinPoint joinPoint) throws Throwable{
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        TrackDBTime annotation = method.getAnnotation(TrackDBTime.class);

        String operation = annotation.operation();

        Timer.Sample sample = Timer.start(meterRegistry);

        try{
            Object result = joinPoint.proceed();

            sample.stop(Timer.builder("db.operation.duration")
                    .tag("operation", operation)
                    .tag("status","success")
                    .description("DB operation execution time")
                    .register(meterRegistry));
            return result;
        } catch (Throwable e){
            sample.stop(Timer.builder("db.operation.duration")
                    .tag("operation",operation)
                    .tag("status","failure")
                    .description("DB operation execution time")
                    .register(meterRegistry));

            Counter.builder("db.operation.failure")
                    .tag("operation", operation)
                    .tag("exception", e.getClass().getSimpleName())
                    .description("Failed DB operations")
                    .register(meterRegistry)
                    .increment();

            throw e;
        }
    }

}

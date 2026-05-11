package com.example.webdev_hardware_store

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class WebdevHardwareStoreApplication

fun main(args: Array<String>) {
    runApplication<WebdevHardwareStoreApplication>(*args)
}

package com.minetoblend.osugachabot

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
    fromApplication<OsugachabotApplication>().with(TestcontainersConfiguration::class).run(*args)
}

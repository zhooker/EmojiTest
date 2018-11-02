package com.example.topic.coroutines

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 *
 * @author zhuangsj
 * @created 2018/11/1
 */

fun main() {
    GlobalScope.launch {
        // launch new coroutine in background and continue
        delay(500L) // 无阻塞的等待1秒钟(默认时间单位是毫秒)
        println("World!") // 在延迟后打印输出
    }
    println("Hello,") // 主线程的协程将会继续等待
    Thread.sleep(800L) // 阻塞主线程2秒钟来保证JVM存活
}

class People(var age: Int = 0) {

    operator fun plus(p: People): People {
        return People(p.age + this.age)
    }

    override fun toString(): String {
        return "People(age=$age)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as People

        if (age != other.age) return false

        return true
    }

    override fun hashCode(): Int {
        return age
    }


}
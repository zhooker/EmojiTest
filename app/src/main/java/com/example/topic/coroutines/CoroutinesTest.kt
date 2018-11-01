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
    GlobalScope.launch { // launch new coroutine in background and continue
        delay(1000L) // 无阻塞的等待1秒钟(默认时间单位是毫秒)
        println("World!") // 在延迟后打印输出
    }
    println("Hello,") // 主线程的协程将会继续等待
    Thread.sleep(2000L) // 阻塞主线程2秒钟来保证JVM存活
}
package com.novus.salat.mongodb

import com.mongodb.{MongoOptions, ServerAddress}
import com.mongodb.casbah.commons.Logging

import java.net.Socket
import java.io.BufferedInputStream

import org.apache.commons.pool._
import org.apache.commons.pool.impl._

case class MongoConnectionKey(addr: ServerAddress, opts: MongoOptions) {
  def connect = MongoConnection(addr, opts)
}

object MongoConnectionKey {
  def apply(host: String, port: Int): MongoConnectionKey = apply(host, port, new MongoOptions)
  def apply(host: String, port: Int, opts: MongoOptions): MongoConnectionKey = MongoConnectionKey(new ServerAddress(host, port), opts)
}

case class MongoConnection(addr: ServerAddress, opts: MongoOptions) {
  lazy val sock = {
    val s = new Socket
    s.connect(addr.getSocketAddress, opts.connectTimeout)
    s.setTcpNoDelay(true)
    s.setSoTimeout(opts.socketTimeout)
    s
  }
  lazy val in = new BufferedInputStream(sock.getInputStream)
  lazy val out = sock.getOutputStream
  def close = try { sock.close } catch { case _ => }
}

object `package` {
  lazy val connectionFactory: KeyedPoolableObjectFactory = {
    new BaseKeyedPoolableObjectFactory with Logging {
      def makeObject(key: AnyRef) = key match {
        case mkey: MongoConnectionKey => mkey.connect
      }
      override def destroyObject(key: AnyRef, obj: AnyRef) = obj match {
	case conn: MongoConnection => conn.close
	case _ =>
      }
    }
  }
  lazy val pool: KeyedObjectPool = new StackKeyedObjectPool(connectionFactory)
  def withMongoConnection[T](key: MongoConnectionKey)(fun: MongoConnection => T) = {
    val conn = pool.borrowObject(key).asInstanceOf[MongoConnection]
    val result = try {
      fun(conn)
    } finally {
      pool.returnObject(key, conn)
    }
    result
  }
}

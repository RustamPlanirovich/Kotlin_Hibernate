package hit

import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.cfg.Configuration
import org.hibernate.cfg.Environment
import java.util.*
import javax.persistence.*


@Entity
@Table(name = "user")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int?,
    val name: String,
    val age: Int,
    @Column(name = "occupation")
    val occupation: String,

    @OneToMany(cascade = arrayOf(CascadeType.ALL))
    @JoinColumn(name = "user_id")
    val address: Set<Address>? = null,

    @OneToMany(cascade = arrayOf(CascadeType.ALL))
    @JoinColumn(name = "auto_id")
    val auto: Set<Auto>? = null
)

@Entity
@Table(name = "address")
data class Address(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    val id: Int?,
    val city: String,
    val street: String,
    val number: String
) {
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    var user: User? = null
}

@Entity
@Table(name = "auto")
data class Auto(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int?,
    val number: String,
    val maker: String,
    val age: Int,
    val run: Int
) {
    @ManyToOne
    @JoinColumn(name = "auto_id", nullable = false)
    var user: User? = null
}


//fun propertiesFromResource(resource: String): Properties {
//    val properties = Properties()
//    properties.load(Any::class.java.getResourceAsStream(resource))
//    return properties
//}

fun Properties.toHibernateProperties(): Properties {
    val hibernateProperties = Properties()
    hibernateProperties[Environment.DRIVER] = this["driver"]
    hibernateProperties[Environment.URL] = this["url"]
    hibernateProperties[Environment.USER] = this["user"]
    hibernateProperties[Environment.PASS] = this["pass"]
    hibernateProperties[Environment.DIALECT] = this["dialect"]
    hibernateProperties[Environment.SHOW_SQL] = this["showSql"]
    hibernateProperties[Environment.FORMAT_SQL] = this["formatSql"]
    hibernateProperties[Environment.CURRENT_SESSION_CONTEXT_CLASS] = this["currentSessionContextClass"]
    hibernateProperties[Environment.HBM2DDL_AUTO] = this["ddlAuto"]

    //C3PO
    hibernateProperties["hibernate.c3p0.min_size"] = this["hibernate.c3p0.min_size"]
    hibernateProperties["hibernate.c3p0.max_size"] = this["hibernate.c3p0.max_size"]
    hibernateProperties["hibernate.c3p0.timeout"] = this["hibernate.c3p0.timeout"]
    hibernateProperties["hibernate.c3p0.max_statements"] = this["hibernate.c3p0.max_statements"]

    return hibernateProperties
}

fun buildHibernateConfiguration(hibernateProperties: Properties, vararg annotatedClasses: Class<*>): Configuration {
    val configuration = Configuration()
    configuration.properties = hibernateProperties
    annotatedClasses.forEach { configuration.addAnnotatedClass(it) }
    return configuration
}

fun <T> SessionFactory.transaction(block: (session: Session) -> T): T {
    val session = openSession()
    val transaction = session.beginTransaction()

    return try {
        val rs = block.invoke(session)
        transaction.commit()
        rs
    } catch (e: Exception) {
        // logger.error("Transaction failed! Rolling back...", e)
        println(
            "Transaction failed! Rolling back... ${e.message}"
        )
        throw e
    }
}

fun addHibernateShutdownHook(sessionFactory: SessionFactory) {
    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            println(
                "Closing the sessionFactory..."
            )

            // logger.debug("Closing the sessionFactory...")
            sessionFactory.close()
            println(
                "sessionFactory closed successfully..."
            )
            // logger.info("sessionFactory closed successfully...")
        }
    })
}

fun buildSessionFactory(configuration: Configuration): SessionFactory {
    val serviceRegistry = StandardServiceRegistryBuilder().applySettings(configuration.properties).build()
    return configuration.buildSessionFactory(serviceRegistry)
}


fun main() {
    //val properties = propertiesFromResource("/database.properties")

    val hibernateProperties = Properties()
    hibernateProperties[Environment.DRIVER] = "com.mysql.cj.jdbc.Driver"
    hibernateProperties[Environment.URL] =
        "jdbc:mysql://localhost:3306/test?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC&useSSL=false"
    hibernateProperties[Environment.USER] = "root"
    hibernateProperties[Environment.PASS] = "Israr@1990"
    hibernateProperties[Environment.DIALECT] = "org.hibernate.dialect.MySQLDialect"
    hibernateProperties[Environment.SHOW_SQL] = true
    hibernateProperties[Environment.FORMAT_SQL] = true
    hibernateProperties[Environment.CURRENT_SESSION_CONTEXT_CLASS] = "thread"
    hibernateProperties[Environment.HBM2DDL_AUTO] = "none"

    //C3PO
//    hibernateProperties["hibernate.c3p0.min_size"] = this["hibernate.c3p0.min_size"]
//    hibernateProperties["hibernate.c3p0.max_size"] = this["hibernate.c3p0.max_size"]
//    hibernateProperties["hibernate.c3p0.timeout"] = this["hibernate.c3p0.timeout"]
//    hibernateProperties["hibernate.c3p0.max_statements"] = this["hibernate.c3p0.max_statements"]
//


    val configuration = buildHibernateConfiguration(
        hibernateProperties,
        //properties.toHibernateProperties(),
        User::class.java,
        Address::class.java,
        Auto::class.java
    )

    val sessionFactory = buildSessionFactory(configuration)

//    sessionFactory.transaction { session ->
//        val user = session.createQuery("from User").uniqueResult() as User
//        println(user)
//
//        val misha = User(null, "Dima Petrov", 34, "medic")
//        session.save(misha)
//
//    }
//    sessionFactory.transaction { session ->
//
//        //создадим адреса
//        val address = mutableSetOf<Address>()
//        address.add(Address(null, "Moscow", "Radio", "12"))
//        address.add(Address(null, "Minsk", "Pobeda", "33"))
//
//        //создадим пользователя
//        val user = User(null, "Mihail Svetlov", 36, "driver", address)
//
//        //сохранить пользователя
//        session.save(user)
//
//        //сохранить адрес
//        address.forEach {
//            it.user = user
//            session.save(it)
//        }
//    }
//    sessionFactory.transaction { session ->
//
//        //создадим адреса
//        val address = mutableSetOf<Address>()
//        address.add(Address(null, "Moscow", "Leninsky", "65"))
//        address.add(Address(null, "Kiev", "Bankova", "1"))
//
//        val auto = mutableSetOf<Auto>()
//        auto.add(Auto(null, "a123fd", "Toyota", 2,100_000))
//        auto.add(Auto(null, "d852es", "Suzuki", 6, 800_000))
//
//        //создадим пользователя
//        val user = User(null, "Mashd Svetlovd", 27, "programmer", address, auto)
//
//        //сохранить адрес
//        address.forEach {
//            it.user = user
//        }
//
//        auto.forEach {
//            it.user = user
//        }
//
//        //сохранить пользователя
//        session.save(user)
//
//
//    }

    sessionFactory.transaction { session ->
        val query: TypedQuery<User> = session.createQuery("select t from User t where id=:id", User::class.java)
        query.setParameter("id", 17)
        val user = query.singleResult
        println(user)
    }


}
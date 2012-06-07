import java.io.Serializable
import java.lang.Long
import java.util._
import jp.rough_diamond.commons.di.DIContainerTestingHelper
import jp.rough_diamond.commons.di.DIContainerTestingHelper.DIHook
import jp.rough_diamond.commons.extractor.{FreeFormat, ExtractValue, Extractor}
import jp.rough_diamond.commons.resource._
import jp.rough_diamond.commons.service.annotation._
import jp.rough_diamond.commons.service.BasicService.RecordLock
import jp.rough_diamond.commons.service.{WhenVerifier, FindResult, BasicService}
import org.scalatest.matchers.ShouldMatchers
import org.scalatest.{BeforeAndAfter, FunSpec}

/**
 * Created with IntelliJ IDEA.
 * User: e-yamane
 * Date: 12/06/06
 * Time: 13:38
 * To change this template use File | Settings | File Templates.
 */

class BasicServiceSpec extends FunSpec with ShouldMatchers with BeforeAndAfter {
    import DIContainerTestingHelper._

    before {
        init()
        attach(diListener)
    }

    after {
        detach(diListener)
    }

    describe("BasicServiceの仕様") {
        it("findByPKの省略値[isNoCache=false、lockはNONE]であること") {
            var service = new BasicServiceExt
            service.findByPK(classOf[String], "a");
            service.isNoCacheParam should be (false)
            service.lockParam should be (RecordLock.NONE)

            service.findByPK(classOf[String], "a", true)
            service.isNoCacheParam should be (true)
            service.lockParam should be (RecordLock.NONE)

            service.findByPK(classOf[String], "a", RecordLock.FOR_UPDATE)
            service.isNoCacheParam should be (false)
            service.lockParam should be (RecordLock.FOR_UPDATE)
        }
        it("findByExtractorの省略値[isNoCache=false、lockはNONE]であること") {
            var service = new BasicServiceExt
            var ex = new Extractor(classOf[String])

            service.findByExtractor(ex);
            service.isNoCacheParam should be (false)
            service.lockParam should be (RecordLock.NONE)

            service.findByExtractor(ex, true)
            service.isNoCacheParam should be (true)
            service.lockParam should be (RecordLock.NONE)

            service.findByExtractor(ex, RecordLock.FOR_UPDATE)
            service.isNoCacheParam should be (false)
            service.lockParam should be (RecordLock.FOR_UPDATE)
        }

        it("findByExtractorWithCountの省略値[isNoCache=false、lockはNONE]であること") {
            var service = new BasicServiceExt
            var ex = new Extractor(classOf[String])

            service.findByExtractorWithCount(ex);
            service.isNoCacheParam should be (false)
            service.lockParam should be (RecordLock.NONE)

            service.findByExtractorWithCount(ex, true)
            service.isNoCacheParam should be (true)
            service.lockParam should be (RecordLock.NONE)

            service.findByExtractorWithCount(ex, RecordLock.FOR_UPDATE)
            service.isNoCacheParam should be (false)
            service.lockParam should be (RecordLock.FOR_UPDATE)
        }

        it("findAllの省略値[isNoCache=false、lockはNONE, fetchSize=DEFAULT_FETCH_SIZE]であること") {
            var service = new BasicServiceExt

            service.findAll(classOf[String])
            service.isNoCacheParam should be (false)
            service.lockParam should be (RecordLock.NONE)
            service.fetchSizeParm should be (Extractor.DEFAULT_FETCH_SIZE)

            service.findAll(classOf[String], true)
            service.isNoCacheParam should be (true)
            service.lockParam should be (RecordLock.NONE)
            service.fetchSizeParm should be (Extractor.DEFAULT_FETCH_SIZE)

            service.findAll(classOf[String], RecordLock.FOR_UPDATE)
            service.isNoCacheParam should be (false)
            service.lockParam should be (RecordLock.FOR_UPDATE)
            service.fetchSizeParm should be (Extractor.DEFAULT_FETCH_SIZE)

            service.findAll(classOf[String], 100)
            service.isNoCacheParam should be (false)
            service.lockParam should be (RecordLock.NONE)
            service.fetchSizeParm should be (100)

            service.findAll(classOf[String], true, RecordLock.FOR_UPDATE)
            service.isNoCacheParam should be (true)
            service.lockParam should be (RecordLock.FOR_UPDATE)
            service.fetchSizeParm should be (Extractor.DEFAULT_FETCH_SIZE)

            service.findAll(classOf[String], true, 100)
            service.isNoCacheParam should be (true)
            service.lockParam should be (RecordLock.NONE)
            service.fetchSizeParm should be (100)

            service.findAll(classOf[String], 100, RecordLock.FOR_UPDATE)
            service.isNoCacheParam should be (false)
            service.lockParam should be (RecordLock.FOR_UPDATE)
            service.fetchSizeParm should be (100)
        }

        it("findByExtractorでExtractorにReturnTypeの指定があればそのタイプをreturnTypeとすること") {
            var service = new BasicServiceExt
            var ex = new Extractor(classOf[String])
            ex.setReturnType(classOf[Long]);
            service.findByExtractor(ex);
            service.typeParam should be (classOf[Long])
        }

        it("findByExtractorでExtractorにExtractValueを１つ以上指定していればMapがreturnTypeであること") {
            var service = new BasicServiceExt
            var ex = new Extractor(classOf[String])
            ex.addExtractValue(new ExtractValue("foo", new FreeFormat("1")))
            service.findByExtractor(ex);
            service.typeParam should be (classOf[Map[_, _]])
        }

        it("findByExtractorでreturnTypeもExtractValueも指定されていない場合はExtractor生成時のタイプがreturnTypeであること") {
            var service = new BasicServiceExt
            var ex = new Extractor(classOf[String])
            service.findByExtractor(ex);
            service.typeParam should be (classOf[String])
        }

        it("findByExtractorWithCountはfindByExtractorの戻り値とgetCountByExtractorの戻り値をがっちゃんこしたものである事") {
            var service = new BasicServiceExt {
                @Override
                override protected def findByExtractor[T](`type`: Class[T], extractor: Extractor, isNoCache: Boolean, lock: RecordLock) : List[T] = {
                    val ret = new ArrayList[Long]
                    ret.add(1L)
                    ret.add(3L)
                    ret.asInstanceOf[List[T]]
                }

                @Override
                override def getCountByExtractor[T](extractor: Extractor)  = {100L}
            }
            var ex = new Extractor(classOf[Long])
            val result = service.findByExtractorWithCount(ex)
            result.list should be (Arrays.asList(1L, 3L).asInstanceOf[List[Long]])
            result.count should be (100L)
        }

        it("deleteByExtractorはfindByExtractorの返却オブジェクトを削除している事") {
            var service = new BasicServiceExt {
                @Override
                override protected def findByExtractor[T](`type`: Class[T], extractor: Extractor, isNoCache: Boolean, lock: RecordLock) : List[T] = {
                    val ret = new ArrayList[Long]
                    ret.add(1L)
                    ret.add(6L)
                    ret.add(5L)
                    ret.asInstanceOf[List[T]]
                }
            }
            var ex = new Extractor(classOf[Long])
            service.deleteByExtractor(ex)
            service.deleteObjects should be (Array(1L, 6L, 5L))
        }

        it("deleteAllはfindAllの返却オブジェクトを削除している事") {
            var service = new BasicServiceExt {
                @Override
                override def findAll[T](`type`: Class[T]) : List[T] = {
                    val ret = new ArrayList[Long]
                    ret.add(1L)
                    ret.add(6L)
                    ret.add(13L)
                    ret.asInstanceOf[List[T]]
                }
            }
            service.deleteAll(classOf[Long])
            service.deleteObjects should be (Array(1L, 6L, 13L))
        }

        it("deleteByPKはfindByPKの返却オブジェクトを削除している事") {
            var service = new BasicServiceExt {
                @Override
                override def findByPK[T](`type`: Class[T], pk: Serializable, isNoCache: Boolean, lock: RecordLock) : T = {
                    if(pk == 1) {
                        null.asInstanceOf[T]
                    } else {
                        "hoge".asInstanceOf[T]
                    }
                }
            }
            service.deleteByPK(classOf[String], 1)
            service.deleteObjects should be (null)
            service.deleteByPK(classOf[String], 2)
            service.deleteObjects should be (Array("hoge"))
        }

        describe("Verifierの仕様") {
            describe("単項目チェックの仕様") {
                it("getterに@NotNullアノテーションが付与されているプロパティは省略不能である事") {
                    val bean = new ValidateTestBean
                    val service = new BasicServiceExt

                    var msg = service.validate(bean, WhenVerifier.INSERT)
                    msg.hasError should be (true)
                    msg.get("Z.reqInfo").size() should be (1)
                    msg.get("Z.reqInfo").get(0).getKey should be ("errors.required")
                    msg.get("Z.reqInfo").get(0).values should be (Array("required Info"))

                    bean.reqInfo = ""
                    msg = service.validate(bean, WhenVerifier.INSERT)
                    msg.hasError should be (true)

                    bean.reqInfo = "x"
                    msg = service.validate(bean, WhenVerifier.INSERT)
                    msg.hasError should be (false)
                }

                it("getterに@MaxLengthアノテーションが付与されているプロパティはその長さを超えない事") {
                    val bean = new ValidateTestBean
                    val service = new BasicServiceExt

                    bean.reqInfo = "x"

                    bean.name = "12345"
                    var msg = service.validate(bean, WhenVerifier.INSERT)
                    msg.hasError should be (false)

                    bean.name = "123456"
                    msg = service.validate(bean, WhenVerifier.INSERT)
                    msg.hasError should be (true)
                    msg.get("Z.name").size() should be (1)
                    msg.get("Z.name").get(0).getKey should be ("errors.maxlength")
                    msg.get("Z.name").get(0).values should be (Array("BeanName", "5"))

                    bean.name = "12345"

                    bean.code = 99
                    msg = service.validate(bean, WhenVerifier.INSERT)
                    msg.hasError should be (false)

                    bean.code = 100
                    msg = service.validate(bean, WhenVerifier.INSERT)
                    msg.hasError should be (true)
                    msg.hasError should be (true)
                    msg.get("Z.code").size() should be (1)
                    msg.get("Z.code").get(0).getKey should be ("errors.maxlength")
                    msg.get("Z.code").get(0).values should be (Array("BeanCode", "2"))
                }
                it("数値で負数の場合の@MaxLengthの挙動は直す方が良い")(pending)

                it("getterに@MaxCharLengthアノテーションが付与されているプロパティはその長さを超えない事") {
                    val bean = new ValidateTestBean
                    val service = new BasicServiceExt

                    bean.reqInfo = "x"

                    bean.charLength = "あ"
                    var msg = service.validate(bean, WhenVerifier.INSERT)
                    msg.hasError should be (false)

                    bean.charLength = "あい"
                    msg = service.validate(bean, WhenVerifier.INSERT)
                    msg.hasError should be (true)
                    msg.get("Z.charLength").size() should be (1)
                    msg.get("Z.charLength").get(0).getKey should be ("errors.maxcharlength")
                    msg.get("Z.charLength").get(0).values should be (Array("length check", "1"))
                }

                it("MaxLengthとMaxCharLength両方違反した場合はMaxCharLengthのエラーを優先する事") {
                    val bean = new ValidateTestBean
                    val service = new BasicServiceExt

                    bean.reqInfo = "x"

                    bean.charLength = "あいうえお"
                    var msg = service.validate(bean, WhenVerifier.INSERT)
                    msg.hasError should be (true)
                    msg.get("Z.charLength").size() should be (1)
                    msg.get("Z.charLength").get(0).getKey should be ("errors.maxcharlength")
                }
                it("複数プロパティでエラーが生じた場合は全て含まれている事") {
                    val bean = new ValidateTestBean
                    val service = new BasicServiceExt

                    bean.name = "123456"
                    bean.code = 100
                    bean.charLength = "あいうえお"
                    val msg = service.validate(bean, WhenVerifier.INSERT)
                    msg.hasError should be (true)
                    msg.get("Z.reqInfo").size() should be (1)
                    msg.get("Z.reqInfo").get(0).getKey should be ("errors.required")
                    msg.get("Z.name").size() should be (1)
                    msg.get("Z.name").get(0).getKey should be ("errors.maxlength")
                    msg.get("Z.code").size() should be (1)
                    msg.get("Z.code").get(0).getKey should be ("errors.maxlength")
                    msg.get("Z.charLength").size() should be (1)
                    msg.get("Z.charLength").get(0).getKey should be ("errors.maxcharlength")
                }
                it("NestedComponentとNotNullチェックが混在している場合NotNullチェックが優先される事") {
                    val bean = new ValidateTestBeanOuter
                    val service = new BasicServiceExt

                    val msg = service.validate(bean, WhenVerifier.INSERT)
                    msg.hasError should be (true)
                    msg.get("Y.bean").size() should be (1)
                    msg.get("Y.bean").get(0).getKey should be ("errors.required")
                }
                it("NestedComponentが付与されているプロパティは再帰的に検証できる事") {
                    val bean = new ValidateTestBeanOuter
                    val service = new BasicServiceExt
                    bean.bean = new ValidateTestBean
                    bean.name = "1234"

                    val msg = service.validate(bean, WhenVerifier.INSERT)
                    msg.hasError should be (true)
                    println(msg.getProperties)
                    msg.get("Y.bean.reqInfo").size() should be (1)
                    msg.get("Y.bean.reqInfo").get(0).getKey should be ("errors.required")
                    msg.get("Y.name").size() should be (1)
                    msg.get("Y.name").get(0).getKey should be ("errors.maxlength")
                }
            }
            describe("CustomeValidate（同一Entity）の仕様") {
                it("INSERTの検証はINSERTのもののみpriorityの降順である事") {
                    val bean = new ValidateTestBean
                    val service = new BasicServiceExt

                    bean.reqInfo = "x"
                    val msg = service.validate(bean, WhenVerifier.INSERT)

                    bean.stack.size() should be (4)
                    bean.stack.toArray should be (Array("2", "3", "4", "1"))
                }
                it("UPDATEの時はUPDATEのもののみでpriorityの降順である事") {
                    val bean = new ValidateTestBean
                    val service = new BasicServiceExt

                    bean.reqInfo = "x"
                    val msg = service.validate(bean, WhenVerifier.UPDATE)

                    bean.stack.size() should be (4)
                    bean.stack.toArray should be (Array("2", "3", "5", "1"))
                }
                it("forceExec=falseもしくは省略した場合、エラーが発生していればvalidateを中止する事") {
                    val bean = new ValidateTestBean
                    val service = new BasicServiceExt

                    bean.reqInfo = "x"
                    bean.name = "foo"
                    val msg = service.validate(bean, WhenVerifier.UPDATE)

                    bean.stack.size() should be (2)
                    bean.stack.toArray should be (Array("2", "3"))
                }
                it("forceExec=trueの場合、エラーが発生してもvalidateは継続する事") {
                    val bean = new ValidateTestBean
                    val service = new BasicServiceExt

                    bean.reqInfo = "x"
                    bean.code = 27
                    bean.name = "foo"
                    val msg = service.validate(bean, WhenVerifier.UPDATE)

                    bean.stack.size() should be (2)
                    bean.stack.toArray should be (Array("2", "3"))

                    msg.get("Z.code").size() should be (1)
                    msg.get("Z.name").size() should be (1)
                }
                it("NestedComponentのValidateはOuterのvalidate後に呼び出される事") {
                    val bean = new ValidateTestBeanOuter
                    val service = new BasicServiceExt

                    bean.bean = new ValidateTestBean
                    val msg = service.validate(bean, WhenVerifier.UPDATE)

                    msg.hasError should be (true)

                    println(bean.bean.stack)

                    bean.bean.stack.size() should be (3)
                    bean.bean.stack.toArray should be (Array("outer", "2", "3"))
                }
            }
        }
    }

    class ValidateTestBean {
        var name : String = null
        var code : Integer = null
        var reqInfo : String = null
        var charLength : String = null

        @MaxLength(length = 5, property = "Z.name")
        def getName : String = {
            name
        }

        @MaxLength(length=2, property="Z.code")
        def getCode : Integer = {
            code
        }

        @MaxCharLength(length = 1, property="Z.charLength")
        @MaxLength(length=6, property="Z.charLength")
        def getCharLength = {
            charLength
        }

        @NotNull(property = "Z.reqInfo")
        def getReqInfo : String = {
            reqInfo
        }

        val stack = new Stack[String]

        @Verifier(priority = 1, isForceExec = false)
        def validate1 : Messages = {
            stack.push("1")
            new Messages
        }

        @Verifier(priority = 10, isForceExec = true)
        def validate2 : Messages = {
            stack.push("2")
            val ret = new Messages()
            if(code == 27) {
                ret.add("Z.code", new Message("Z.code"))
            }
            return ret
        }

        @Verifier(priority = 5, isForceExec = true)
        def validate3 : Messages = {
            stack.push("3")
            val ret = new Messages()
            if(name == "foo") {
                ret.add("Z.name", new Message("Z.name"))
            }
            return ret
        }

        @Verifier(priority = 4, when=Array(WhenVerifier.INSERT))
        def validate4 : Messages = {
            stack.push("4")
            new Messages
        }

        @Verifier(priority = 4, when=Array(WhenVerifier.UPDATE))
        def validate5 : Messages = {
            stack.push("5")
            new Messages
        }
    }

    class ValidateTestBeanOuter {
        var bean : ValidateTestBean = null
        var name : String = null

        @NotNull(property = "Y.bean")
        @NestedComponent(property = "Y.bean")
        def getBean = {
            bean
        }

        @MaxLength(length = 3, property = "Y.name")
        def getName : String = {
            name
        }

        @Verifier(isForceExec = true)
        def validateOuter : Messages = {
            if(bean != null) {
                bean.stack.push("outer")
            }
            return new Messages
        }
    }

    class BasicServiceExt extends BasicService {
        var isNoCacheParam : Boolean = true
        var lockParam : RecordLock = RecordLock.FOR_UPDATE
        var typeParam : Class[_] = null
        var fetchSizeParm : Long = null
        var deleteObjects : Array[_] = null

        def findByPK[T](`type`: Class[T], pk: Serializable, isNoCache: Boolean, lock: RecordLock) = {
            isNoCacheParam = isNoCache
            lockParam = lock
            null.asInstanceOf[T]
        }

        protected def findByExtractor[T](`type`: Class[T], extractor: Extractor, isNoCache: Boolean, lock: RecordLock) :List[T] = {
            isNoCacheParam = isNoCache
            lockParam = lock
            typeParam = `type`
            fetchSizeParm = extractor.getFetchSize
            new ArrayList[T]
        }

        def getCountByExtractor[T](extractor: Extractor) = 0L

        def replaceProxy[T](t: T) = null.asInstanceOf[T]

        def insert[T](objects: T*) :Unit = {}

        def update[T](objects: T*) :Unit = {}

        def delete(objects: AnyRef*) :Unit = {
            deleteObjects = objects.toArray
        }

        def clearCache(o: Any) :Unit = {}

        protected def getProxyChecker = null
    }

    object diListener extends DIHook {
        lazy val lc = new LocaleControllerByThreadLocal

        def getObject[T](`type`: Class[T], key: Any) : T = {
            key match {
                case BasicService.CHARSET_KEY => "UTF-8".asInstanceOf[T]
                case BasicService.PERSISTENCE_EVENT_LISTENERS => new ArrayList[Object].asInstanceOf[T]
                case LocaleController.LOCALE_CONTROLLER_KEY => lc.asInstanceOf[T]
                case ResourceManager.RESOURCE_NAME_KEY => "a".asInstanceOf[T]
                case _ => null.asInstanceOf[T]
            }
        }
    }
}

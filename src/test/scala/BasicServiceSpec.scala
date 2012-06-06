import java.io.Serializable
import java.lang.Long
import java.util.{Arrays, Map, ArrayList, List}
import jp.rough_diamond.commons.extractor.{FreeFormat, ExtractValue, Extractor}
import jp.rough_diamond.commons.service.BasicService.RecordLock
import jp.rough_diamond.commons.service.{FindResult, BasicService}
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
}

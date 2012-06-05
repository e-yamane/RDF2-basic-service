import java.util.{Collections, Arrays}
import jp.rough_diamond.commons.service.annotation.Verifier
import jp.rough_diamond.commons.service.VerifierSorter
import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

/**
 * Created with IntelliJ IDEA.
 * User: e-yamane
 * Date: 12/06/05
 * Time: 16:57
 * To change this template use File | Settings | File Templates.
 */

class VerifierSorterSpec extends FunSpec with ShouldMatchers {
    describe("VerifierSorterの仕様") {
        it("priorityの高い方が先に来る") {
            val mFoo = this.getClass.getMethod("foo");
            val mBar = this.getClass.getMethod("bar");
            val list = Arrays.asList(mFoo, mBar);
            Collections.sort(list, new VerifierSorter);
            list.get(0) should be(mBar)
            list.get(1) should be(mFoo)
        }
        it("priorityが同じ場合は文字列表現の小さい順にソートされる") {
            val mBoo = this.getClass.getMethod("boo");
            val mBar = this.getClass.getMethod("bar");
            val list = Arrays.asList(mBoo, mBar);
            Collections.sort(list, new VerifierSorter);
            list.get(0) should be(mBar)
            list.get(1) should be(mBoo)
        }
    }

    @Verifier(priority = 1)
    def foo(): Unit = {}

    @Verifier(priority = 2)
    def boo() : Unit = {}

    @Verifier(priority = 2)
    def bar(): Unit = {}
}

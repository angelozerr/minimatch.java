/**
 * Copyright (c) 2015 Angelo ZERR
 *
 * All rights reserved.
 *
 * Permission is hereby granted, free  of charge, to any person obtaining
 * a  copy  of this  software  and  associated  documentation files  (the
 * "Software"), to  deal in  the Software without  restriction, including
 * without limitation  the rights to  use, copy, modify,  merge, publish,
 * distribute,  sublicense, and/or sell  copies of  the Software,  and to
 * permit persons to whom the Software  is furnished to do so, subject to
 * the following conditions:
 *
 * The  above  copyright  notice  and  this permission  notice  shall  be
 * included in all copies or substantial portions of the Software.
 *
 * THE  SOFTWARE IS  PROVIDED  "AS  IS", WITHOUT  WARRANTY  OF ANY  KIND,
 * EXPRESS OR  IMPLIED, INCLUDING  BUT NOT LIMITED  TO THE  WARRANTIES OF
 * MERCHANTABILITY,    FITNESS    FOR    A   PARTICULAR    PURPOSE    AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE,  ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package minimatch;

public class Options {

	public static final Options DEFAULT = new Options();

	private boolean allowWindowsPaths;
	private boolean nocomment;
	private boolean nonegate;
	private boolean nobrace;
	private boolean noglobstar;
	private boolean nocase;
	private boolean dot;
	private boolean noext;
	private boolean matchBase;
	private boolean flipNegate;

	private Debugger debugger;

	public boolean isAllowWindowsPaths() {
		return allowWindowsPaths;
	}

	public void setAllowWindowsPaths(boolean allowWindowsPaths) {
		this.allowWindowsPaths = allowWindowsPaths;
	}

	public boolean isNocomment() {
		return nocomment;
	}

	public void setNocomment(boolean nocomment) {
		this.nocomment = nocomment;
	}

	public boolean isNonegate() {
		return nonegate;
	}

	public void setNonegate(boolean nonegate) {
		this.nonegate = nonegate;
	}

	public boolean isNobrace() {
		return nobrace;
	}

	public void setNobrace(boolean nobrace) {
		this.nobrace = nobrace;
	}

	public boolean isNoglobstar() {
		return noglobstar;
	}

	public void setNoglobstar(boolean noglobstar) {
		this.noglobstar = noglobstar;
	}

	public boolean isNocase() {
		return nocase;
	}

	public void setNocase(boolean nocase) {
		this.nocase = nocase;
	}

	public boolean isDot() {
		return dot;
	}

	public void setDot(boolean dot) {
		this.dot = dot;
	}

	public boolean isNoext() {
		return noext;
	}

	public void setNoext(boolean noext) {
		this.noext = noext;
	}

	public boolean isDebug() {
		return debugger != null;
	}

	public boolean isMatchBase() {
		return matchBase;
	}

	public void setMatchBase(boolean matchBase) {
		this.matchBase = matchBase;
	}

	public boolean isFlipNegate() {
		return flipNegate;
	}

	public void setFlipNegate(boolean flipNegate) {
		this.flipNegate = flipNegate;
	}

	public Debugger getDebugger() {
		return debugger;
	}

	public void setDebugger(Debugger debugger) {
		this.debugger = debugger;
	}

}

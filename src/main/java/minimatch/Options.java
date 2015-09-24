/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Angelo
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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

	public Options setAllowWindowsPaths(boolean allowWindowsPaths) {
		this.allowWindowsPaths = allowWindowsPaths;
		return this;
	}

	public boolean isNocomment() {
		return nocomment;
	}

	public Options setNocomment(boolean nocomment) {
		this.nocomment = nocomment;
		return this;
	}

	public boolean isNonegate() {
		return nonegate;
	}

	public Options setNonegate(boolean nonegate) {
		this.nonegate = nonegate;
		return this;
	}

	public boolean isNobrace() {
		return nobrace;
	}

	public Options setNobrace(boolean nobrace) {
		this.nobrace = nobrace;
		return this;
	}

	public boolean isNoglobstar() {
		return noglobstar;
	}

	public Options setNoglobstar(boolean noglobstar) {
		this.noglobstar = noglobstar;
		return this;
	}

	public boolean isNocase() {
		return nocase;
	}

	public Options setNocase(boolean nocase) {
		this.nocase = nocase;
		return this;
	}

	public boolean isDot() {
		return dot;
	}

	public Options setDot(boolean dot) {
		this.dot = dot;
		return this;
	}

	public boolean isNoext() {
		return noext;
	}

	public Options setNoext(boolean noext) {
		this.noext = noext;
		return this;
	}

	public boolean isDebug() {
		return debugger != null;
	}

	public boolean isMatchBase() {
		return matchBase;
	}

	public Options setMatchBase(boolean matchBase) {
		this.matchBase = matchBase;
		return this;
	}

	public boolean isFlipNegate() {
		return flipNegate;
	}

	public Options setFlipNegate(boolean flipNegate) {
		this.flipNegate = flipNegate;
		return this;
	}

	public Debugger getDebugger() {
		return debugger;
	}

	public Options setDebugger(Debugger debugger) {
		this.debugger = debugger;
		return this;
	}
	
	@Override
	@SuppressWarnings("nls")
	public String toString() {
		StringBuilder sb = new StringBuilder();
		appendIfTrue(sb, "allowWindowsPaths", allowWindowsPaths);
		appendIfTrue(sb, "nocomment", nocomment);
		appendIfTrue(sb, "nonegate", nonegate);
		appendIfTrue(sb, "nobrace", nobrace);
		appendIfTrue(sb, "noglobstar", noglobstar);
		appendIfTrue(sb, "nocase", nocase);
		appendIfTrue(sb, "dot", dot);
		appendIfTrue(sb, "noext", noext);
		appendIfTrue(sb, "matchBase", matchBase);
		appendIfTrue(sb, "flipNegate", flipNegate);
		if (sb.length() > 0) {
			sb.insert(0, "[");
			sb.setLength(sb.length() - 2);
			sb.append("]");
			return sb.toString();
		} else {
			return "[]";
		}
	}
	
	private void appendIfTrue(StringBuilder str, String name, boolean value) {
		if (value) {
			str.append(name);
			str.append("=true, "); //$NON-NLS-1$
		}
	}

}

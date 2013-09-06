// ListIterator.java, created Wed Mar  5  0:26:32 2003 by joewhaley
// Copyright (C) 2001-3 John Whaley <jwhaley@alum.mit.edu>
// Licensed under the terms of the GNU LGPL; see COPYING for details.
package joeq.Util.Templates;

/**
 * @author John Whaley <jwhaley@alum.mit.edu>
 * @version $Id: ListIterator.java,v 1.6 2004/03/09 21:56:18 jwhaley Exp $
 */
public abstract class ListIterator {
    public interface jq_Type extends java.util.ListIterator {
        joeq.Class.jq_Type nextType();
        joeq.Class.jq_Type previousType();
    }
    public interface jq_Reference extends jq_Type {
        joeq.Class.jq_Reference nextReference();
        joeq.Class.jq_Reference previousReference();
    }
    public interface jq_Class extends jq_Reference {
        joeq.Class.jq_Class nextClass();
        joeq.Class.jq_Class previousClass();
    }
    public interface jq_Member extends java.util.ListIterator {
        joeq.Class.jq_Member nextMember();
        joeq.Class.jq_Member previousMember();
    }
    public interface jq_Method extends jq_Member {
        joeq.Class.jq_Method nextMethod();
        joeq.Class.jq_Method previousMethod();
    }
    public interface jq_InstanceMethod extends jq_Method {
        joeq.Class.jq_InstanceMethod nextInstanceMethod();
        joeq.Class.jq_InstanceMethod previousInstanceMethod();
    }
    public interface jq_StaticMethod extends jq_Method {
        joeq.Class.jq_StaticMethod nextStaticMethod();
        joeq.Class.jq_StaticMethod previousStaticMethod();
    }
        
    public interface BasicBlock extends java.util.ListIterator {
        joeq.Compiler.Quad.BasicBlock nextBasicBlock();
        joeq.Compiler.Quad.BasicBlock previousBasicBlock();
    }
    public interface ExceptionHandler extends java.util.ListIterator {
        joeq.Compiler.Quad.ExceptionHandler nextExceptionHandler();
        joeq.Compiler.Quad.ExceptionHandler previousExceptionHandler();
    }
    public interface Quad extends java.util.ListIterator {
        joeq.Compiler.Quad.Quad nextQuad();
        joeq.Compiler.Quad.Quad previousQuad();
    }
    public interface Operand extends java.util.ListIterator {
        joeq.Compiler.Quad.Operand nextOperand();
        joeq.Compiler.Quad.Operand previousOperand();
    }
    public interface RegisterOperand extends Operand {
        joeq.Compiler.Quad.Operand.RegisterOperand nextRegisterOperand();
        joeq.Compiler.Quad.Operand.RegisterOperand previousRegisterOperand();
    }
}

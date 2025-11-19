/*
 * Copyright (c) 2019-2025 Bigraph Toolkit Suite Developers
 * Main Developer: Dominik Grzelak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bigraphs.framework.core;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import org.bigraphs.framework.core.datatypes.FiniteOrdinal;
import org.bigraphs.framework.core.datatypes.NamedType;
import org.bigraphs.framework.core.utils.emf.EMFUtils;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

/**
 * Abstract class for signatures defined via Ecore.
 *
 * @param <C> type of control
 * @author Dominik Grzelak
 */
public abstract class AbstractEcoreSignature<C extends Control<? extends NamedType, ? extends FiniteOrdinal>>
        implements Signature<C>, EcoreSignature {

    // Collection of controls of type C
    protected Set<C> controls;

    protected EPackage sigPackage;
    protected EObject instanceModel;

    int hashed = -1;

    protected AbstractEcoreSignature() {
        this.controls = new LinkedHashSet<>();
    }

    protected AbstractEcoreSignature(EObject signatureInstanceModel) {
        this();
        assert signatureInstanceModel != null;
        this.instanceModel = signatureInstanceModel;
        this.sigPackage = this.instanceModel.eClass().getEPackage();
        assert this.sigPackage != null;

        recreateControls();
        recreateSorts();
    }

    protected AbstractEcoreSignature(Set<C> controls) {
        this.controls = controls;
    }

    @Override
    public Set<C> getControls() {
        return controls;
    }

    @Override
    public abstract EPackage getMetaModel();

    @Override
    public abstract EObject getInstanceModel();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractEcoreSignature)) return false;
        AbstractEcoreSignature<?> that = (AbstractEcoreSignature<?>) o;
        return controls.equals(that.controls);
    }

    protected void recreateControls() {

    }

    protected void recreateSorts() {

    }

    @Override
    public int hashCode() {
        if (hashed == -1) {
            hashed = Objects.hash(controls);
        }
        return hashed;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" +
                "controls=" + controls +
                '}';
    }

    // ///////////////////////////////////////////
    // Some helper methods for Ecore-related stuff
    // ///////////////////////////////////////////

    protected EClass extendBKindSortCompositeEClass(String controlName, EPackage sigPackage) {
        EClassifier eClassifier = sigPackage.getEClassifier(BigraphMetaModelConstants.SignaturePackage.ECLASS_KINDSORTNONATOMIC);
        EClass kindOfcontrolClass = EMFUtils.createEClass(controlName);
        EMFUtils.addSuperType(kindOfcontrolClass, (EClass) eClassifier);
        sigPackage.getEClassifiers().add(kindOfcontrolClass);
        return kindOfcontrolClass;
    }

    protected EClass extendBKindSortLeafEClass(String controlName, EPackage sigPackage) {
        EClassifier eClassifier = sigPackage.getEClassifier(BigraphMetaModelConstants.SignaturePackage.ECLASS_BKINDSORTATOMIC);
        EClass kindOfControlClass = EMFUtils.createEClass(controlName);
        EMFUtils.addSuperType(kindOfControlClass, (EClass) eClassifier);
        sigPackage.getEClassifiers().add(kindOfControlClass);
        return kindOfControlClass;
    }

    protected EClass extendBControlEClass(String newControlName, EPackage sigPackage) {
        EClassifier eClassifier = sigPackage.getEClassifier(BigraphMetaModelConstants.SignaturePackage.ECLASS_BCONTROL);
        EClass controlClass = EMFUtils.createEClass(newControlName);
        EMFUtils.addSuperType(controlClass, (EClass) eClassifier);
        sigPackage.getEClassifiers().add(controlClass);
        return controlClass;
    }
}

package de.tudresden.inf.st.bigraphs.core;

public abstract class BigraphHandler {

//    default boolean areConnected(BigraphEntity.NodeEntity place1, BigraphEntity.NodeEntity place2) {
////        EStructuralFeature portsRef = place1.getInstance().eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_PORT);
////        if (Objects.isNull(portsRef)) return false;
////        EList<EObject> bPorts = (EList<EObject>) place1.getInstance().eGet(portsRef);
////        for (EObject bPort : bPorts) {
////            EStructuralFeature linkRef = bPort.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_LINK);
////            if (Objects.isNull(linkRef)) return false;
////            EObject linkObject = (EObject) bPort.eGet(linkRef);
////            if (Objects.isNull(linkObject)) continue;
////            EStructuralFeature pointsRef = linkObject.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_POINT);
////            if (Objects.isNull(pointsRef)) continue;
////            EList<EObject> bPoints = (EList<EObject>) linkObject.eGet(pointsRef);
////            for (EObject bPoint : bPoints) {
////                if (isBPort(bPoint)) {
////                    EStructuralFeature nodeRef = bPoint.eClass().getEStructuralFeature(BigraphMetaModelConstants.REFERENCE_NODE);
////                    assert nodeRef != null;
////                    if (bPoint.eGet(nodeRef).equals(place2.getInstance())) {
////                        return true;
////                    }
////                }
////            }
////        }
//        return false;
//    }

//    default boolean isBPort(EObject eObject) {
//        return eObject.eClass().getClassifierID() ==
//                (((EPackageImpl) getModelPackage()).getEClassifierGen(BigraphMetaModelConstants.CLASS_PORT)).getClassifierID() ||
//                eObject.eClass().getEAllSuperTypes().contains(((EPackageImpl) getModelPackage()).getEClassifierGen(BigraphMetaModelConstants.CLASS_PORT));
//    }
//
//    default boolean isBNode(EObject eObject) {
//        return eObject.eClass().equals(((EPackageImpl) getModelPackage()).getEClassifierGen(BigraphMetaModelConstants.CLASS_NODE)) ||
//                eObject.eClass().getEAllSuperTypes().contains(((EPackageImpl) getModelPackage()).getEClassifierGen(BigraphMetaModelConstants.CLASS_NODE));
//    }

    //    public BigraphEntity findByNodeName(Collection<BigraphEntity> list, String nodeName) {
//        return list.stream().filter(x -> BigraphEntityType.isNode(x)
//                && ((BigraphEntity.NodeEntity<Object>) x).getName().equals(nodeName))
//                .findFirst()
//                .get();
//    }
//
//    public BigraphEntity findRootByIndex(Collection<BigraphEntity.RootEntity> list, int index) {
//        return list.stream().filter(x -> x.getIndex() == index)
//                .findFirst()
//                .get();
//    }
}

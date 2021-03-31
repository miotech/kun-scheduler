package com.miotech.kun.workflow.executor.kubernetes.mock;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.authentication.TokenReview;
import io.fabric8.kubernetes.api.model.certificates.v1beta1.CertificateSigningRequest;
import io.fabric8.kubernetes.api.model.certificates.v1beta1.CertificateSigningRequestList;
import io.fabric8.kubernetes.api.model.coordination.v1.Lease;
import io.fabric8.kubernetes.api.model.coordination.v1.LeaseList;
import io.fabric8.kubernetes.api.model.node.v1beta1.RuntimeClass;
import io.fabric8.kubernetes.api.model.node.v1beta1.RuntimeClassList;
import io.fabric8.kubernetes.client.AdmissionRegistrationAPIGroupDSL;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.VersionInfo;
import io.fabric8.kubernetes.client.dsl.*;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.dsl.internal.RawCustomResourceOperationsImpl;
import io.fabric8.kubernetes.client.extended.leaderelection.LeaderElectorBuilder;
import io.fabric8.kubernetes.client.extended.run.RunOperations;
import io.fabric8.kubernetes.client.informers.SharedInformerFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.ExecutorService;

public class MockClient implements KubernetesClient {
    @Override
    public ApiextensionsAPIGroupDSL apiextensions() {
        return null;
    }

    @Override
    public NonNamespaceOperation<CertificateSigningRequest, CertificateSigningRequestList, Resource<CertificateSigningRequest>> certificateSigningRequests() {
        return null;
    }

    @Override
    public CertificatesAPIGroupDSL certificates() {
        return null;
    }

    @Override
    public <T extends CustomResource> MixedOperation<T, KubernetesResourceList<T>, Resource<T>> customResources(Class<T> resourceType) {
        return null;
    }

    @Override
    public <T extends CustomResource, L extends KubernetesResourceList<T>> MixedOperation<T, L, Resource<T>> customResources(Class<T> resourceType, Class<L> listClass) {
        return null;
    }

    @Override
    public <T extends HasMetadata, L extends KubernetesResourceList<T>> MixedOperation<T, L, Resource<T>> customResources(CustomResourceDefinitionContext crdContext, Class<T> resourceType, Class<L> listClass) {
        return null;
    }

    @Override
    public DiscoveryAPIGroupDSL discovery() {
        return null;
    }

    @Override
    public ExtensionsAPIGroupDSL extensions() {
        return null;
    }

    @Override
    public VersionInfo getVersion() {
        return null;
    }

    @Override
    public RawCustomResourceOperationsImpl customResource(CustomResourceDefinitionContext customResourceDefinition) {
        return null;
    }

    @Override
    public AdmissionRegistrationAPIGroupDSL admissionRegistration() {
        return null;
    }

    @Override
    public AppsAPIGroupDSL apps() {
        return null;
    }

    @Override
    public AutoscalingAPIGroupDSL autoscaling() {
        return null;
    }

    @Override
    public NetworkAPIGroupDSL network() {
        return null;
    }

    @Override
    public StorageAPIGroupDSL storage() {
        return null;
    }

    @Override
    public BatchAPIGroupDSL batch() {
        return null;
    }

    @Override
    public MetricAPIGroupDSL top() {
        return null;
    }

    @Override
    public PolicyAPIGroupDSL policy() {
        return null;
    }

    @Override
    public RbacAPIGroupDSL rbac() {
        return null;
    }

    @Override
    public SchedulingAPIGroupDSL scheduling() {
        return null;
    }

    @Override
    public MixedOperation<ComponentStatus, ComponentStatusList, Resource<ComponentStatus>> componentstatuses() {
        return null;
    }

    @Override
    public ParameterNamespaceListVisitFromServerGetDeleteRecreateWaitApplicable<HasMetadata> load(InputStream is) {
        return null;
    }

    @Override
    public ParameterNamespaceListVisitFromServerGetDeleteRecreateWaitApplicable<HasMetadata> resourceList(String s) {
        return null;
    }

    @Override
    public NamespaceListVisitFromServerGetDeleteRecreateWaitApplicable<HasMetadata> resourceList(KubernetesResourceList list) {
        return null;
    }

    @Override
    public NamespaceListVisitFromServerGetDeleteRecreateWaitApplicable<HasMetadata> resourceList(HasMetadata... items) {
        return null;
    }

    @Override
    public NamespaceListVisitFromServerGetDeleteRecreateWaitApplicable<HasMetadata> resourceList(Collection<HasMetadata> items) {
        return null;
    }

    @Override
    public <T extends HasMetadata> NamespaceVisitFromServerGetWatchDeleteRecreateWaitApplicable<T> resource(T is) {
        return null;
    }

    @Override
    public NamespaceVisitFromServerGetWatchDeleteRecreateWaitApplicable<HasMetadata> resource(String s) {
        return null;
    }

    @Override
    public MixedOperation<Binding, KubernetesResourceList<Binding>, Resource<Binding>> bindings() {
        return null;
    }

    @Override
    public MixedOperation<Endpoints, EndpointsList, Resource<Endpoints>> endpoints() {
        return null;
    }

    @Override
    public NonNamespaceOperation<Namespace, NamespaceList, Resource<Namespace>> namespaces() {
        return null;
    }

    @Override
    public NonNamespaceOperation<Node, NodeList, Resource<Node>> nodes() {
        return null;
    }

    @Override
    public NonNamespaceOperation<PersistentVolume, PersistentVolumeList, Resource<PersistentVolume>> persistentVolumes() {
        return null;
    }

    @Override
    public MixedOperation<PersistentVolumeClaim, PersistentVolumeClaimList, Resource<PersistentVolumeClaim>> persistentVolumeClaims() {
        return null;
    }

    @Override
    public MixedOperation<Pod, PodList, PodResource<Pod>> pods() {
        return new MockOperation();
    }

    @Override
    public MixedOperation<ReplicationController, ReplicationControllerList, RollableScalableResource<ReplicationController>> replicationControllers() {
        return null;
    }

    @Override
    public MixedOperation<ResourceQuota, ResourceQuotaList, Resource<ResourceQuota>> resourceQuotas() {
        return null;
    }

    @Override
    public MixedOperation<Secret, SecretList, Resource<Secret>> secrets() {
        return null;
    }

    @Override
    public MixedOperation<Service, ServiceList, ServiceResource<Service>> services() {
        return null;
    }

    @Override
    public MixedOperation<ServiceAccount, ServiceAccountList, Resource<ServiceAccount>> serviceAccounts() {
        return null;
    }

    @Override
    public MixedOperation<APIService, APIServiceList, Resource<APIService>> apiServices() {
        return null;
    }

    @Override
    public KubernetesListMixedOperation lists() {
        return null;
    }

    @Override
    public MixedOperation<ConfigMap, ConfigMapList, Resource<ConfigMap>> configMaps() {
        return null;
    }

    @Override
    public MixedOperation<LimitRange, LimitRangeList, Resource<LimitRange>> limitRanges() {
        return null;
    }

    @Override
    public AuthorizationAPIGroupDSL authorization() {
        return null;
    }

    @Override
    public Createable<TokenReview> tokenReviews() {
        return null;
    }

    @Override
    public SharedInformerFactory informers() {
        return null;
    }

    @Override
    public SharedInformerFactory informers(ExecutorService executorService) {
        return null;
    }

    @Override
    public <C extends Namespaceable<C> & KubernetesClient> LeaderElectorBuilder<C> leaderElector() {
        return null;
    }

    @Override
    public MixedOperation<Lease, LeaseList, Resource<Lease>> leases() {
        return null;
    }

    @Override
    public V1APIGroupDSL v1() {
        return null;
    }

    @Override
    public RunOperations run() {
        return null;
    }

    @Override
    public NonNamespaceOperation<RuntimeClass, RuntimeClassList, Resource<RuntimeClass>> runtimeClasses() {
        return null;
    }

    @Override
    public <C> Boolean isAdaptable(Class<C> type) {
        return null;
    }

    @Override
    public <C> C adapt(Class<C> type) {
        return null;
    }

    @Override
    public URL getMasterUrl() {
        return null;
    }

    @Override
    public String getApiVersion() {
        return null;
    }

    @Override
    public String getNamespace() {
        return null;
    }

    @Override
    public RootPaths rootPaths() {
        return null;
    }

    @Override
    public boolean supportsApiPath(String path) {
        return false;
    }

    @Override
    public void close() {

    }

    @Override
    public Config getConfiguration() {
        return null;
    }
}

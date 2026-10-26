import { createApp } from 'vue'
import { createRouter, createWebHistory } from 'vue-router'
import App from './App.vue'
import ReviewView from './views/ReviewView.vue'
import ReplyView from './views/ReplyView.vue'
import BranchView from './views/BranchView.vue'
import AutoView from './views/AutoView.vue'
import TextView from './views/TextView.vue'
import ExtractView from './views/ExtractView.vue'
import ReplanView from './views/ReplanView.vue'
import UtilityView from './views/UtilityView.vue'
import StageView from './views/StageView.vue'
import ReflectView from './views/ReflectView.vue'
import NamedView from './views/NamedView.vue'
import MathView from './views/MathView.vue'
import StampView from './views/StampView.vue'
import UnfoldView from './views/UnfoldView.vue'
import GateView from './views/GateView.vue'
import RoleView from './views/RoleView.vue'
import FallbackView from './views/FallbackView.vue'
import ScatterView from './views/ScatterView.vue'
import ConsensusView from './views/ConsensusView.vue'
import GuideView from './views/GuideView.vue'
import PrefetchView from './views/PrefetchView.vue'
import FilterView from './views/FilterView.vue'
import UrtextView from './views/UrtextView.vue'
import MemoryView from './views/MemoryView.vue'
import SimpleLineView from './views/SimpleLineView.vue'
import FormView from './views/FormView.vue'
import SubagentView from './views/SubagentView.vue'
import ExportView from './views/ExportView.vue'
import EchoView from './views/EchoView.vue'
import StyleGuideView from './views/StyleGuideView.vue'
import ProcessesView from './views/ProcessesView.vue'
import McpView from './views/McpView.vue'
import './styles.css'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/reviews' },
    { path: '/reviews', component: ReviewView },
    { path: '/replies', component: ReplyView },
    { path: '/branches', component: BranchView },
    { path: '/auto', component: AutoView },
    { path: '/text', component: TextView },
    { path: '/extract', component: ExtractView },
    { path: '/replan', component: ReplanView },
    { path: '/utility', component: UtilityView },
    { path: '/stages', component: StageView },
    { path: '/reflect', component: ReflectView },
    { path: '/logs', component: NamedView },
    { path: '/math', component: MathView },
    { path: '/stamps', component: StampView },
    { path: '/unfold', component: UnfoldView },
    { path: '/gates', component: GateView },
    { path: '/roles', component: RoleView },
    { path: '/fallbacks', component: FallbackView },
    { path: '/scatters', component: ScatterView },
    { path: '/consensus', component: ConsensusView },
    { path: '/guide-lines', component: GuideView },
    { path: '/prefetches', component: PrefetchView },
    { path: '/filters', component: FilterView },
    { path: '/urtexts', component: UrtextView },
    { path: '/memories', component: MemoryView },
    { path: '/simple-lines', component: SimpleLineView },
    { path: '/forms', component: FormView },
    { path: '/subagents', component: SubagentView },
    { path: '/exports', component: ExportView },
    { path: '/echoes', component: EchoView },
    { path: '/style-guide', component: StyleGuideView },
    { path: '/processes', component: ProcessesView },
    { path: '/mcp', component: McpView },
  ],
})

createApp(App).use(router).mount('#app')

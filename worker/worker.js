// Import the compiled Kotlin/JS worker
import kotlinModule from './build/compileSync/js/main/productionExecutable/kotlin/KotlinWorker-worker.js';

// Export the ES module format expected by Cloudflare Workers
export default {
  async fetch(request, env, ctx) {
    try {
      // Access the Kotlin fetch function from the module exports
      // Avoid naming conflict with global fetch by using kotlinModule
      if (kotlinModule && typeof kotlinModule.fetch === 'function') {
        console.log('Calling Kotlin fetch function');
        return await kotlinModule.fetch(request, env, ctx);
      } else {
        console.error('Kotlin fetch function not found in module exports');
        return new Response('Kotlin worker function not available', { status: 500 });
      }
    } catch (error) {
      console.error('Error in worker:', error);
      return new Response(`Worker error: ${error.message}`, { status: 500 });
    }
  }
};